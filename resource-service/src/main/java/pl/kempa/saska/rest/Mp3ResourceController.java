package pl.kempa.saska.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.GetObjectRequest;

import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.ApiErrorDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceIdListDTO;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.dto.StorageType;
import pl.kempa.saska.feign.client.SongsApiClient;
import pl.kempa.saska.feign.client.StoragesApiClient;
import pl.kempa.saska.rest.exception.IncorrectIdParamException;
import pl.kempa.saska.rest.validator.Mp3ResourceValidator;
import pl.kempa.saska.service.Mp3ResourceDBService;
import pl.kempa.saska.service.Mp3ResourceS3Service;
import pl.kempa.saska.service.RabbitMQService;

@RestController
@RequestMapping(value = "/api/resources")
@Slf4j
public class Mp3ResourceController {

  @Autowired private Mp3ResourceS3Service s3Service;
  @Autowired private Mp3ResourceDBService mp3ResourceDBService;
  @Autowired private Mp3ResourceValidator validator;
  @Autowired private RabbitMQService rabbitMQService;
  @Autowired private StoragesApiClient storagesApiClient;
  @Autowired private SongsApiClient songsApiClient;
  @Autowired private RetryTemplate retryTemplate;

  @GetMapping
  public ResponseEntity<List<Mp3ResourceInfoDTO>> getAll() {
    return ResponseEntity.ok(mp3ResourceDBService.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> downloadById(@PathVariable Integer id, @RequestHeader HttpHeaders headers)
      throws IOException {
    List<HttpRange> ranges = headers.getRange();
    Optional<Mp3ResourceInfoDTO> resourceInfoDTO = mp3ResourceDBService.getByResourceId(id);
    if (resourceInfoDTO.isEmpty()) {
      return ResponseEntity.notFound()
          .build();
    }
    Optional<StorageDTO> storageDTO = resourceInfoDTO.map(Mp3ResourceInfoDTO::getStorageId)
        .map(storagesApiClient::getStoragesById);
    if (resourceInfoDTO.isEmpty()) {
      return ResponseEntity.internalServerError()
          .body(new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, "Storage for the resource can't be found"));
    }
    InputStream inputStream = storageDTO.map(s -> new GetObjectRequest(s.getBucket(), s.getPath()
            .concat(id.toString())))
        .map(request -> s3Service.download(request, ranges))
        .get();
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(inputStream.readAllBytes());
  }

  @PostMapping
  public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
    Optional<ApiErrorDTO> error = validator.validate(file);
    if (error.isPresent()) {
      return ResponseEntity.badRequest()
          .body(error.get());
    }
    // 1) get STAGING storage info
    var stagingStorageOpt = storagesApiClient.getStoragesByType(StorageType.STAGING.name())
        .stream()
        .findFirst();
    if (stagingStorageOpt.isEmpty()) {
      return ResponseEntity.internalServerError()
          .body(new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, "Can't save file to storage, try later"));
    }
    // 2) upload file
    var resourceIdDTO = stagingStorageOpt.flatMap(s -> s3Service.upload(file, s));
    var storageDTO = stagingStorageOpt.get();
    log.info("{} was uploaded to STAGING storage {}", file.getName(), storageDTO.getBucket());
    // 3) save resource info to DB
    resourceIdDTO.map(Mp3ResourceIdDTO::getId)
        .map(resourceId -> new Mp3ResourceInfoDTO(null, resourceId, file.getSize(), storageDTO.getId()))
        .ifPresent(mp3ResourceDBService::save);
    // 4) send message to song-service that mp3 resource is uploaded
    resourceIdDTO.ifPresent(rabbitMQService::mp3ResourceUploadMessageSend);
    return resourceIdDTO.map(ResponseEntity::ok)
        .orElseThrow(() -> new RuntimeException("Something went wrong"));
  }

  @DeleteMapping
  public ResponseEntity<?> delete(@RequestParam String id) {
    Optional<ApiErrorDTO> error = validator.validate(id);
    if (error.isPresent()) {
      return ResponseEntity.badRequest()
          .body(error.get());
    }
    try {
      Set<Integer> idSet = Stream.of(id.split(","))
          .map(String::trim)
          .map(Integer::parseInt)
          .collect(Collectors.toSet());
      List<Integer> deletedMp3Ids = idSet.stream()
          // 1) get from DB -> get storage
          .map(mp3ResourceDBService::getByResourceId)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(resourceInfoDTO -> {
            // 2) delete from S3
            var storage = storagesApiClient.getStoragesById(resourceInfoDTO.getStorageId());
            String key = storage.getPath()
                .concat(resourceInfoDTO.getResourceId()
                    .toString());
            s3Service.delete(key, storage.getBucket());
            // 3) delete delete in song service with retryTemplate
            songsApiClient.deleteMp3Details(resourceInfoDTO.getResourceId());
            // 4) delete from DB
            mp3ResourceDBService.delete(resourceInfoDTO.getResourceId());
            log.info("{} was deleted everywhere", id);
            return resourceInfoDTO;
          })
          .map(Mp3ResourceInfoDTO::getResourceId)
          .toList();
      return ResponseEntity.ok(new Mp3ResourceIdListDTO(deletedMp3Ids));
    } catch (NumberFormatException e) {
      throw new IncorrectIdParamException(String.format("Id param %s should contain only numbers", id));
    }
  }
}
