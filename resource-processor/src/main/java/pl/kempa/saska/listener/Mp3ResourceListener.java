package pl.kempa.saska.listener;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.Mp3DetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.processor.Mp3DetailsProcessor;
import pl.kempa.saska.util.WebClientUtil;

@Service
@Slf4j
@AllArgsConstructor
public class Mp3ResourceListener {

  private Mp3DetailsProcessor processor;

  private WebClientUtil webClientUtil;

  private RetryTemplate retryTemplate;

  @RabbitListener(queues = "${spring.rabbitmq.queue}")
  public void onMp3ResourceUpload(Mp3ResourceIdDTO resourceIdDTO)
      throws TikaException, IOException, SAXException {
    byte[] mp3Resource = retryTemplate.execute(context -> {
      log.info("call Get Mp3Resource attempt!");
      return webClientUtil.callGetMp3Resource(resourceIdDTO.getId());
    });
    Mp3DetailsDTO mp3DetailsDTO =
        processor.processMp3(new ByteArrayInputStream(mp3Resource), resourceIdDTO.getId());
    Mp3ResourceIdDTO mp3ResourceIdDTO =
        retryTemplate.execute(context -> {
          log.info("call Save Mp3Details attempt!");
          return webClientUtil.callSaveMp3Details(mp3DetailsDTO);
        });
    log.info("Details were saved for Mp3Resource with resourceId=" + mp3ResourceIdDTO.getId());
  }
}
