package pl.kempa.saska.listener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.tika.exception.TikaException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.Mp3DetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.feign.client.ResourcesApiClient;
import pl.kempa.saska.feign.client.SongsApiClient;
import pl.kempa.saska.processor.Mp3DetailsProcessor;
import pl.kempa.saska.service.RabbitMQService;

@Component
@Slf4j
public class Mp3ResourceUploadedListener {

  private Mp3DetailsProcessor processor;
  @LoadBalanced
  private SongsApiClient songsApiClient;
  private ResourcesApiClient resourcesApiClient;
  private RetryTemplate retryTemplate;
  private RabbitMQService rabbitMQService;
  private final AtomicLong fileSize;

  public Mp3ResourceUploadedListener(Mp3DetailsProcessor processor, SongsApiClient songsApiClient,
                                     ResourcesApiClient resourcesApiClient,
                                     RetryTemplate retryTemplate, RabbitMQService rabbitMQService,
                                     MeterRegistry meterRegistry) {
    this.processor = processor;
    this.songsApiClient = songsApiClient;
    this.resourcesApiClient = resourcesApiClient;
    this.retryTemplate = retryTemplate;
    this.rabbitMQService = rabbitMQService;
    this.fileSize = meterRegistry.gauge("mp3.file.size", new AtomicLong(0));
  }

  @Timed("process.mp3.full")
  @RabbitListener(queues = "${spring.rabbitmq.queue.resource-uploaded}")
  public void onMp3ResourceUploaded(Mp3ResourceIdDTO resourceIdDTO)
      throws TikaException, IOException, SAXException {
    byte[] mp3Resource = retryTemplate.execute(context -> resourcesApiClient.getMp3Resource(
        resourceIdDTO.getId()
            .toString()));
    fileSize.set(mp3Resource.length);
    Mp3DetailsDTO mp3DetailsDTO =
        processor.processMp3(new ByteArrayInputStream(mp3Resource), resourceIdDTO.getId());
    rabbitMQService.fileProcessedSuccessfullyNotify(
        new Mp3ResourceIdDTO(mp3DetailsDTO.getResourceId()));
    log.info("Call song-service and send resource {} metadata for saving",
        resourceIdDTO.getId());
    retryTemplate.execute(context -> songsApiClient.saveMp3Details(mp3DetailsDTO));
  }
}
