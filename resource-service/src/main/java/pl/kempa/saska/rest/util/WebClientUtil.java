package pl.kempa.saska.rest.util;

import static java.util.Collections.singletonList;
import static pl.kempa.saska.dto.StorageType.PERMANENT;
import static pl.kempa.saska.dto.StorageType.STAGING;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.dto.StorageType;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class WebClientUtil {

  private WebClient.Builder webClientBuilder;

  @CircuitBreaker(name = "storage-service", fallbackMethod = "getFallbackByType")
  public List<StorageDTO> callGetStoragesByType(StorageType type) {
    return webClientBuilder.build()
        .get()
        .uri("http://storage-service/api/storages?storageType=" + type.name())
        .retrieve()
        .onStatus(status -> status.equals(HttpStatus.NOT_FOUND),
            response -> Mono.empty())
        .bodyToMono(new ParameterizedTypeReference<List<StorageDTO>>() {
        })
        .block();
  }

  public List<StorageDTO> getFallbackByType(StorageType type, RuntimeException exception) {
    log.warn("!!! CircuitBreaker getFallbackByType() call !!!");
    var storage = type.equals(STAGING) ? getStaging() : getPermanent();
    log.warn("!!! Return the stub DTO {}", storage);
    return singletonList(storage);
  }

  @CircuitBreaker(name = "storage-service", fallbackMethod = "getFallbackById")
  public StorageDTO callGetStoragesById(Integer id) {
    return webClientBuilder.build()
        .get()
        .uri("http://storage-service/api/storages/" + id)
        .retrieve()
        .onStatus(status -> status.equals(HttpStatus.NOT_FOUND),
            response -> Mono.empty())
        .bodyToMono(StorageDTO.class)
        .block();
  }

  public StorageDTO getFallbackById(Integer id, RuntimeException exception) {
    log.warn("!!! CircuitBreaker getFallbackById() call !!!");
    var storage = id == 1 ? getStaging() : getPermanent();
    log.warn("!!! Return the stub DTO {}", storage);
    return storage;
  }

  private StorageDTO getStaging() {
    return StorageDTO.builder()
        .id(1)
        .storageType(STAGING)
        .bucket("a-lautsou" +
            "-resources-storage" +
            "-staging")
        .path("files/")
        .build();
  }

  private StorageDTO getPermanent() {
    return StorageDTO.builder()
        .id(2)
        .storageType(PERMANENT)
        .bucket("a-lautsou" +
            "-resources-storage" +
            "-permanent")
        .path("files/")
        .build();
  }
}
