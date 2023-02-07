package pl.kempa.saska.rest.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import pl.kempa.saska.dto.Mp3ResourceDetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.rest.exception.Mp3DetailsNotFoundException;
import reactor.core.publisher.Mono;

@Component
public class WebClientUtil {
  @Autowired private WebClient webClient;

  public boolean callIsMp3Exists(MultipartFile file) {
    return Boolean.TRUE.equals(webClient.get()
        .uri("http://localhost:8082/api/songs?fileName=".concat(file.getOriginalFilename()))
        .retrieve()
        .bodyToMono(Boolean.class)
        .block());
  }

  public Mp3ResourceDetailsDTO callFetchMp3Details(Integer id) {
    return webClient.get()
        .uri("http://localhost:8082/api/songs/" + id)
        .retrieve()
        .onStatus(status -> status.equals(HttpStatus.NOT_FOUND),
            response -> Mono.error(new Mp3DetailsNotFoundException("There is no such mp3 " +
                "resource with Id=" + id)))
        .bodyToMono(Mp3ResourceDetailsDTO.class)
        .block();
  }

  public Mp3ResourceDetailsDTO callParseMp3Details(MultipartFile file) {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", file.getResource());
    return webClient.post()
        .uri("http://localhost:8083/api/mp3-details")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .retrieve()
        .bodyToMono(Mp3ResourceDetailsDTO.class)
        .block();
  }

  public Mp3ResourceIdDTO callSaveMp3Details(Mp3ResourceDetailsDTO mp3DetailsDTO) {
    return webClient.post()
        .uri("http://localhost:8082/api/songs")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(mp3DetailsDTO), Mp3ResourceDetailsDTO.class)
        .retrieve()
        .bodyToMono(Mp3ResourceIdDTO.class)
        .block();
  }

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
