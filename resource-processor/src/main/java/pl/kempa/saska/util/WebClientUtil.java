package pl.kempa.saska.util;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.Mp3DetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class WebClientUtil {

  private WebClient webClient;

  public byte[] callGetMp3Resource(Integer resourceId) {
    return webClient.get()
        .uri("http://localhost:8081/api/resources/".concat(resourceId.toString()))
        .retrieve()
        .bodyToMono(byte[].class)
        .block();
  }

  public Mp3ResourceIdDTO callSaveMp3Details(Mp3DetailsDTO mp3DetailsDTO) {
    return webClient.post()
        .uri("http://localhost:8082/api/songs")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(mp3DetailsDTO), Mp3DetailsDTO.class)
        .retrieve()
        .bodyToMono(Mp3ResourceIdDTO.class)
        .block();
  }
}
