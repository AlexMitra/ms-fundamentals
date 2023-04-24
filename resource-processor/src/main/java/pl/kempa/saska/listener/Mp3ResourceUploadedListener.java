package pl.kempa.saska.listener;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.Mp3DetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.feign.client.ResourcesApiClient;
import pl.kempa.saska.feign.client.SongsApiClient;
import pl.kempa.saska.processor.Mp3DetailsProcessor;
import pl.kempa.saska.service.RabbitMQService;

@Component
@Slf4j
@AllArgsConstructor
public class Mp3ResourceUploadedListener {

  private Mp3DetailsProcessor processor;
  @LoadBalanced
  private SongsApiClient songsApiClient;
  private ResourcesApiClient resourcesApiClient;
  private RetryTemplate retryTemplate;
  private RabbitMQService rabbitMQService;

  @RabbitListener(queues = "${spring.rabbitmq.queue.resource-uploaded}")
  public void onMp3ResourceUpload(Mp3ResourceIdDTO resourceIdDTO)
      throws TikaException, IOException, SAXException {
    byte[] mp3Resource = retryTemplate.execute(context -> resourcesApiClient.getMp3Resource(
        resourceIdDTO.getId()
            .toString()));
    Mp3DetailsDTO mp3DetailsDTO =
        processor.processMp3(new ByteArrayInputStream(mp3Resource), resourceIdDTO.getId());
    rabbitMQService.fileProcessedSuccessfullyNotify(new Mp3ResourceIdDTO(mp3DetailsDTO.getResourceId()));
    Mp3ResourceIdDTO mp3ResourceIdDTO =
        retryTemplate.execute(context -> songsApiClient.saveMp3Details(mp3DetailsDTO));
    log.info("Details were saved for Mp3Resource with resourceId={}", mp3ResourceIdDTO.getId());
  }
}
