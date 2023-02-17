package pl.kempa.saska.converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.dto.Mp3ResourceS3InfoDTO;
import pl.kempa.saska.repository.model.Mp3ResourceInfo;

@Component
public class Mp3ResourceConverter {
  public Mp3ResourceS3InfoDTO toDTO(S3ObjectSummary s3ObjectSummary) {
    return Mp3ResourceS3InfoDTO.builder()
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

  public Mp3ResourceInfoDTO toDTO(Mp3ResourceInfo mp3ResourceInfo) {
    return Mp3ResourceInfoDTO.builder()
        .id(mp3ResourceInfo.getId())
        .resourceId(mp3ResourceInfo.getResourceId())
        .fileSize(mp3ResourceInfo.getFileSize())
        .build();
  }

  public Mp3ResourceInfo toEntity(Mp3ResourceInfoDTO dto) {
    return Mp3ResourceInfo.builder()
        .resourceId(dto.getResourceId())
        .fileSize(dto.getFileSize())
        .build();
  }
}
