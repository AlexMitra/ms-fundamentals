package pl.kempa.saska.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import pl.kempa.saska.repository.model.StorageEntity;

public interface StorageRepository extends CrudRepository<StorageEntity, Integer> {
  Optional<StorageEntity> findByBucketName(String bucketName);

  boolean existsByStorageType(String storageType);

  Iterable<StorageEntity> findAllByStorageType(String storageType);
}
