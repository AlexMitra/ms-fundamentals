package pl.kempa.saska.rest.validator;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import pl.kempa.saska.dto.ApiErrorDTO;

@Component
public class Mp3ResourceValidator {

  private static final int IDS_MAX_LENGTH = 200;

  public Optional<ApiErrorDTO> validate(MultipartFile file) {
    if (file.getSize() == 0 && file.getOriginalFilename()
        .isBlank()) {
      return Optional.of(new ApiErrorDTO(HttpStatus.BAD_REQUEST, "Please select a file"));
    }
    var isAudio = "audio/mpeg".equals(file.getContentType());
    var extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
    var isMp3 = "mp3".equals(extension);
    if (!isAudio || !isMp3) {
      return Optional.of(new ApiErrorDTO(HttpStatus.BAD_REQUEST, "Please select mp3 file"));
    }
    return Optional.empty();
  }

  public Optional<ApiErrorDTO> validate(String ids) {
    if (ids.length() > IDS_MAX_LENGTH) {
      return Optional.of(new ApiErrorDTO(HttpStatus.BAD_REQUEST,
          String.format("Id param can't be more than %d characters", IDS_MAX_LENGTH)));
    }
    return Optional.empty();
  }
}
