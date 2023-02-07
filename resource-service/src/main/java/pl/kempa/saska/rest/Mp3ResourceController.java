package pl.kempa.saska.rest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
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

import pl.kempa.saska.dto.ApiErrorDTO;
import pl.kempa.saska.dto.Mp3ResourceDetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceIdListDTO;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.rest.exception.IncorrectIdParamException;
import pl.kempa.saska.rest.util.WebClientUtil;
import pl.kempa.saska.rest.validator.Mp3ResourceValidator;
import pl.kempa.saska.service.Mp3ResourceS3Service;

@RestController
@RequestMapping(value = "/api/resources")
public class Mp3ResourceController {

  @Autowired private Mp3ResourceS3Service s3Service;
  @Autowired private Mp3ResourceValidator validator;
  @Autowired private WebClientUtil webClientUtil;

  @GetMapping public ResponseEntity<List<Mp3ResourceInfoDTO>> getALl() {
    return ResponseEntity.ok(s3Service.getAll());
  }

  @GetMapping("/{id}") public ResponseEntity<?> downloadById(@PathVariable Integer id,
                                                             @RequestHeader HttpHeaders headers) {
    Optional<Mp3ResourceDetailsDTO> mp3DetailsDTO =
        Optional.ofNullable(webClientUtil.callFetchMp3Details(id));
    List<HttpRange> ranges = headers.getRange();
    return mp3DetailsDTO.map(d -> s3Service.download(d, ranges))
        .map(ResponseEntity::ok)
        .orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND));
  }

  @PostMapping public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
    Optional<ApiErrorDTO> error = validator.validate(file);
    if (error.isPresent()) {
      return ResponseEntity.badRequest()
          .body(error.get());
    }
    if (webClientUtil.callIsMp3Exists(file)) {
      return ResponseEntity.badRequest()
          .body(new ApiErrorDTO(HttpStatus.BAD_REQUEST, "mp3 song has already saved"));
    }
    Mp3ResourceDetailsDTO mp3ResourceDetailsDTO = webClientUtil.callParseMp3Details(file);
    Optional<String> eTag = s3Service.upload(file);
    Optional<Mp3ResourceIdDTO> resourceIdDTO =
        eTag.map(tag -> webClientUtil.callSaveMp3Details(mp3ResourceDetailsDTO));
    if (resourceIdDTO.isEmpty()) {
      var errorDTO =
          new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Something went wrong"));
      s3Service.delete(file.getOriginalFilename());
      return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return ResponseEntity.ok(resourceIdDTO);
  }

  @DeleteMapping public ResponseEntity<?> delete(@RequestParam String id) {
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
