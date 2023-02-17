package pl.kempa.saska.rest.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.Mp3ResourceDetailsDTO;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class WebClientUtil {
  private WebClient webClient;

  public Mp3ResourceDetailsDTO callDeleteMp3Details(Integer id) {
    return webClient.delete()
        .uri("http://localhost:8082/api/songs/" + id)
        .retrieve()
        .onStatus(status -> status.equals(HttpStatus.NOT_FOUND),
            response -> Mono.empty())
        .bodyToMono(Mp3ResourceDetailsDTO.class)
        .block();
  }
}
