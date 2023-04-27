package pl.kempa.saska.service;

import java.util.List;
import java.util.Optional;

import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.dto.StorageIdDTO;
import pl.kempa.saska.dto.StorageType;

public interface StorageService {

  Optional<StorageDTO> getById(Integer id);

  List<StorageDTO> getAll();

  List<StorageDTO> getByStoragesType(String storageType);

  boolean isExistsByStorageType(StorageType storageType);

  Optional<StorageIdDTO> create(StorageDTO storageDTO);

  Optional<StorageIdDTO> delete(Integer id);

}
