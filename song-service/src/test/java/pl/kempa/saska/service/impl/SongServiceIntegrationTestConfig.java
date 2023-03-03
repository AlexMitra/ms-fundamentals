package pl.kempa.saska.service.impl;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pl.kempa.saska.converter.SongConverter;
import pl.kempa.saska.service.SongService;

@Configuration
public class SongServiceIntegrationTestConfig {

  @Bean
  public SongService songService() {
    return new SongServiceImpl();
  }

  @Bean
  public SongConverter converter() {
    return new SongConverter();
  }
}
