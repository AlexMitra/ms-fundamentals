package pl.kempa.saska.converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import pl.kempa.saska.dto.Mp3ResourceInfoDTO;

@Component
public class Mp3ResourceConverter {
  public Mp3ResourceInfoDTO toDTO(S3ObjectSummary s3ObjectSummary) {
    return Mp3ResourceInfoDTO.builder()
        .name(s3ObjectSummary.getKey())
        .size(s3ObjectSummary.getSize())
        .lastModified(convertDateToLocalDateTime(s3ObjectSummary.getLastModified()))
        .build();
  }

  private LocalDateTime convertDateToLocalDateTime(Date date) {
    return Instant.ofEpochMilli(date.getTime())
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
  }
}
