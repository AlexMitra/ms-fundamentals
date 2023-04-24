package pl.kempa.saska.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.converter.StorageConverter;
import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.dto.StorageIdDTO;
import pl.kempa.saska.dto.StorageType;
import pl.kempa.saska.repository.StorageRepository;
import pl.kempa.saska.repository.model.StorageEntity;
import pl.kempa.saska.service.StorageService;
import pl.kempa.saska.service.util.S3Util;

@Service
@AllArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {

  private AmazonS3 s3Client;
  private StorageConverter converter;
  private StorageRepository repository;
  private S3Util s3Util;

  @Override
  public Optional<StorageDTO> getById(Integer id) {
    return repository.findById(id)
        .map(converter::toDTO);
  }

  @Override
  public List<StorageDTO> getAll() {
    List<String> bucketNames = s3Client.listBuckets()
        .stream()
        .map(Bucket::getName)
        .toList();
    Iterable<StorageEntity> storages = repository.findAll();
    return StreamSupport.stream(storages.spliterator(), false)
        .filter(s -> bucketNames.contains(s.getBucketName()))
        .map(converter::toDTO)
        .toList();
  }

  @Override
  public List<StorageDTO> getByStoragesType(String storageType) {
    List<String> bucketNames = s3Client.listBuckets()
        .stream()
        .map(Bucket::getName)
        .toList();
    Iterable<StorageEntity> storages = repository.findAllByStorageType(storageType);
    return StreamSupport.stream(storages.spliterator(), false)
        .filter(s -> bucketNames.contains(s.getBucketName()))
        .map(converter::toDTO)
        .toList();
  }

  public boolean isExistsByStorageType(StorageType storageType) {
    return repository.existsByStorageType(storageType.name());
  }

  @Override
  public Optional<StorageIdDTO> create(StorageDTO storageDTO) {
    if (!s3Client.doesBucketExistV2(storageDTO.getBucket())) {
      try {
        Bucket bucket = s3Client.createBucket(storageDTO.getBucket());
        s3Util.createFolder(bucket, storageDTO);
        repository.save(converter.toEntity(storageDTO));
        return repository.findByBucketName(storageDTO.getBucket())
            .map(StorageEntity::getId)
            .map(StorageIdDTO::new);
      } catch (AmazonS3Exception e) {
        log.error(e.getErrorMessage());
        throw new RuntimeException(e);
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<StorageIdDTO> delete(Integer id) {
    Optional<StorageEntity> storageEntity = repository.findById(id);
    storageEntity.ifPresent(s -> s3Util.deleteBucketContent(s.getBucketName()));
    storageEntity.ifPresent(s -> s3Client.deleteBucket(s.getBucketName()));
    storageEntity.ifPresent(repository::delete);
    return storageEntity.map(s -> new StorageIdDTO(s.getId()));
  }
}
