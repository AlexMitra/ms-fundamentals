package pl.kempa.saska.converter;

import org.springframework.stereotype.Component;

import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.dto.StorageType;
import pl.kempa.saska.repository.model.StorageEntity;

@Component
public class StorageConverter {
  public StorageEntity toEntity(StorageDTO dto) {
    return StorageEntity.builder()
        .storageType(dto.getStorageType()
            .name())
        .bucketName(dto.getBucket())
        .path(
            dto.getPath())
        .build();
  }

  public StorageDTO toDTO(StorageEntity entity) {
    return StorageDTO.builder()
        .id(entity.getId())
        .storageType(StorageType.valueOf(entity.getStorageType()))
        .bucket(
            entity.getBucketName())
        .path(entity.getPath())
        .build();
  }
}
