package pl.kempa.saska.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.service.RabbitMQService;

@Service
@Slf4j
public class RabbitMQServiceImpl implements RabbitMQService {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Value("${spring.rabbitmq.exchange}")
  private String exchange;

  @Value("${spring.rabbitmq.routingkey}")
  private String routingkey;

  @Override
  public void mp3ResourceUploadMessageSend(Mp3ResourceIdDTO resourceIdDTO) {
    rabbitTemplate.convertAndSend(exchange, routingkey, resourceIdDTO);
  }
}
