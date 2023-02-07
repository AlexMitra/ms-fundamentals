package pl.kempa.saska.converter;

import org.springframework.stereotype.Component;

import pl.kempa.saska.dto.SongInfoDTO;
import pl.kempa.saska.repository.model.SongInfo;

@Component
public class SongConverter {
  public SongInfoDTO toDTO(SongInfo entity) {
    return SongInfoDTO.builder()
        .id(entity.getId())
        .fileName(entity.getFileName())
        .fileSize(entity.getFileSize())
        .title(entity.getTitle())
        .artist(entity.getArtist())
        .album(entity.getAlbum())
        .length(entity.getLength())
        .releaseDate(entity.getReleaseDate())
        .build();
  }

  public SongInfo toEntity(SongInfoDTO dto) {
    return SongInfo.builder()
        .fileName(dto.getFileName())
        .fileSize(dto.getFileSize())
        .title(dto.getTitle())
        .artist(dto.getArtist())
        .album(dto.getAlbum())
        .length(dto.getLength())
        .releaseDate(dto.getReleaseDate())
        .build();
  }
}
