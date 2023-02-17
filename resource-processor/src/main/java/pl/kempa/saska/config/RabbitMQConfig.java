package pl.kempa.saska.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {

  @Value("${spring.rabbitmq.host}")
  private String host;

  @Value("${spring.rabbitmq.username}")
  private String username;

  @Value("${spring.rabbitmq.password}")
  private String password;

  @Value("${spring.rabbitmq.exchange}")
  private String exchange;

  @Value("${spring.rabbitmq.routingkey}")
  private String routingKey;

  @Value("${spring.rabbitmq.queue}")
  private String queue;


  @Bean
  Queue mp3UploadNotificationQueue() {
    return new Queue(queue, true);
  }

  @Bean
  Exchange mp3UploadNotificationExchange() {
    return ExchangeBuilder.directExchange(exchange)
        .durable(true)
        .build();
  }

  @Bean
  Binding binding() {
    return BindingBuilder
        .bind(mp3UploadNotificationQueue())
        .to(mp3UploadNotificationExchange())
        .with(routingKey)
        .noargs();
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
    cachingConnectionFactory.setUsername(username);
    cachingConnectionFactory.setPassword(password);
    return cachingConnectionFactory;
  }

  @Bean
  public RetryOperationsInterceptor retryInterceptor() {
    return RetryInterceptorBuilder.stateless()
        .maxAttempts(10)
        .backOffOptions(2000, 3.0, 100000)
        .build();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      SimpleRabbitListenerContainerFactoryConfigurer configurer) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    configurer.configure(factory, connectionFactory());
    factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
    factory.setAdviceChain(retryInterceptor());
    return factory;
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    return rabbitTemplate;
  }
}
