package pl.kempa.saska.converter;

import org.springframework.stereotype.Component;

import pl.kempa.saska.dto.SongInfoDTO;
import pl.kempa.saska.repository.model.SongInfo;

@Component
public class SongConverter {
  public SongInfoDTO toDTO(SongInfo entity) {
    return SongInfoDTO.builder()
        .id(entity.getId())
        .resourceId(entity.getResourceId())
        .title(entity.getTitle())
        .artist(entity.getArtist())
        .album(entity.getAlbum())
        .length(entity.getLength())
        .releaseDate(entity.getReleaseDate())
        .build();
  }

  public SongInfo toEntity(SongInfoDTO dto) {
    return SongInfo.builder()
        .resourceId(dto.getResourceId())
        .title(dto.getTitle())
        .artist(dto.getArtist())
        .album(dto.getAlbum())
        .length(dto.getLength())
        .releaseDate(dto.getReleaseDate())
        .build();
  }
}
