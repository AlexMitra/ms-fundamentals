package pl.kempa.saska.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.service.RabbitMQService;

@Service
public class RabbitMQServiceImpl implements RabbitMQService {
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Value("${spring.rabbitmq.exchange.resource-processed}")
  private String resourceProcessedEx;

  @Value("${spring.rabbitmq.routingkey.resource-processed}")
  private String resourceProcessedRK;

  @Override
  public void fileProcessedSuccessfullyNotify(Mp3ResourceIdDTO resourceIdDTO) {
    rabbitTemplate.convertAndSend(resourceProcessedEx, resourceProcessedRK, resourceIdDTO);
  }
}
