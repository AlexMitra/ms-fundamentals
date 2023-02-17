package pl.kempa.saska.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import pl.kempa.saska.dto.ApiErrorDTO;
import pl.kempa.saska.dto.Mp3ResourceDetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceIdListDTO;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.dto.Mp3ResourceS3InfoDTO;
import pl.kempa.saska.listener.Mp3ResourceDBService;
import pl.kempa.saska.listener.Mp3ResourceS3Service;
import pl.kempa.saska.listener.RabbitMQService;
import pl.kempa.saska.rest.exception.IncorrectIdParamException;
import pl.kempa.saska.rest.util.WebClientUtil;
import pl.kempa.saska.rest.validator.Mp3ResourceValidator;

@RestController
@RequestMapping(value = "/api/resources")
@AllArgsConstructor
public class Mp3ResourceController {

  private Mp3ResourceS3Service s3Service;
  private Mp3ResourceDBService mp3ResourceDBService;
  private Mp3ResourceValidator validator;
  private WebClientUtil webClientUtil;
  private RabbitMQService rabbitMQService;

  @GetMapping
  public ResponseEntity<List<Mp3ResourceS3InfoDTO>> getALl() {
    return ResponseEntity.ok(s3Service.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> downloadById(@PathVariable Integer id,
                                        @RequestHeader HttpHeaders headers)
      throws IOException {
    List<HttpRange> ranges = headers.getRange();
    InputStream inputStream = s3Service.download(id, ranges);
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
    Optional<Mp3ResourceIdDTO> resourceIdDTO = s3Service.upload(file);
    resourceIdDTO.map(Mp3ResourceIdDTO::getId)
        .map(resourceId -> new Mp3ResourceInfoDTO(null,
            resourceId, file.getSize()))
        .ifPresent(mp3ResourceDBService::save);
    resourceIdDTO.ifPresent(rabbitMQService::mp3ResourceUploadMessageSend);
    return resourceIdDTO.map(ResponseEntity::ok)
        .orElseThrow(() -> new RuntimeException("Something went wrong"));
  }

  // todo: update it
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
          .map(i -> {
            Optional<Mp3ResourceDetailsDTO> mp3DetailsDTO =
                Optional.ofNullable(webClientUtil.callDeleteMp3Details(i));
            mp3DetailsDTO.ifPresent(detailsDTO -> s3Service.delete(detailsDTO.getFileName()));
            return mp3DetailsDTO;
          })
          .filter(Optional::isPresent)
          .flatMap(Optional::stream)
          .map(Mp3ResourceDetailsDTO::getId)
          .toList();
      return ResponseEntity.ok(new Mp3ResourceIdListDTO(deletedMp3Ids));
    } catch (NumberFormatException e) {
      throw new IncorrectIdParamException(
          String.format("Id param %s should contain only numbers", id));
    }
  }
}
