package pl.kempa.saska.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;

import pl.kempa.saska.dto.ApiErrorDTO;
import pl.kempa.saska.dto.Mp3ResourceDetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceIdListDTO;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.dto.Mp3ResourceS3InfoDTO;
import pl.kempa.saska.service.Mp3ResourceDBService;
import pl.kempa.saska.service.Mp3ResourceS3Service;
import pl.kempa.saska.service.RabbitMQService;
import pl.kempa.saska.rest.exception.IncorrectIdParamException;
import pl.kempa.saska.rest.util.WebClientUtil;
import pl.kempa.saska.rest.validator.Mp3ResourceValidator;

@RestController
@RequestMapping(value = "/api/resources")
public class Mp3ResourceController {

  @Autowired private Mp3ResourceS3Service s3Service;
  @Autowired private Mp3ResourceDBService mp3ResourceDBService;
  @Autowired private Mp3ResourceValidator validator;
  @Autowired private WebClientUtil webClientUtil;
  @Autowired private RabbitMQService rabbitMQService;
  @Value("${aws.s3.bucket}") private String bucketName;

  @GetMapping
  public ResponseEntity<List<Mp3ResourceS3InfoDTO>> getAll() {
    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);
    return ResponseEntity.ok(s3Service.getAll(listObjectsRequest));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> downloadById(@PathVariable Integer id,
                                        @RequestHeader HttpHeaders headers)
      throws IOException {
    List<HttpRange> ranges = headers.getRange();
    GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, id.toString());
    InputStream inputStream = s3Service.download(getObjectRequest, ranges);
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
    Optional<Mp3ResourceIdDTO> resourceIdDTO = s3Service.upload(file, bucketName);
    resourceIdDTO.map(Mp3ResourceIdDTO::getId)
        .map(resourceId -> new Mp3ResourceInfoDTO(null, resourceId, file.getSize()))
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
            mp3DetailsDTO.ifPresent(
                detailsDTO -> s3Service.delete(detailsDTO.getFileName(), bucketName));
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
