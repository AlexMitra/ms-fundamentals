package pl.kempa.saska.rest;

import static pl.kempa.saska.dto.StorageType.PERMANENT;
import static pl.kempa.saska.dto.StorageType.STAGING;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.ApiErrorDTO;
import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.dto.StorageIdDTO;
import pl.kempa.saska.dto.StorageType;
import pl.kempa.saska.service.StorageService;

@RestController
@RequestMapping(value = "/api/storages")
@AllArgsConstructor
@Slf4j
public class StorageController {

  private static final List<StorageType> limitedStorageTypes = List.of(PERMANENT, STAGING);
  private StorageService service;

  @GetMapping(value = "/{id}")
  public ResponseEntity<?> getStorageById(@PathVariable Integer id) {
    Optional<StorageDTO> storageDTO = service.getById(id);
    if (storageDTO.isEmpty()) {
      return ResponseEntity.notFound()
          .build();
    }
    return storageDTO.map(ResponseEntity::ok)
        .get();
  }

  @GetMapping
  public ResponseEntity<List<StorageDTO>> listStorages(@RequestParam String storageType) {
    var storageTypeOpt = Optional.ofNullable(storageType);
    if (storageTypeOpt.isPresent()) {
      return storageTypeOpt.map(StorageType::valueOfStorage)
          .map(st -> service.getByStoragesType(st.name()))
          .map(ResponseEntity::ok)
          .get();
    }
    return ResponseEntity.ok(service.getAll());
  }

  @PostMapping
  public ResponseEntity<?> createStorage(@RequestBody StorageDTO storageDTO) {
    if (limitedStorageTypes.contains(storageDTO.getStorageType()) && service.isExistsByStorageType(
        storageDTO.getStorageType())) {
      return ResponseEntity.badRequest()
          .body(new ApiErrorDTO(HttpStatus.BAD_REQUEST, String.format("Storage with %s type is already " +
              "exists", storageDTO.getStorageType())));
    }
    return service.create(storageDTO)
        .map(ResponseEntity::ok)
        .get();
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity<?> delete(@PathVariable Integer id) {
    Optional<StorageIdDTO> storageIdDTO = service.delete(id);
    if (storageIdDTO.isEmpty()) {
      return ResponseEntity.notFound()
          .build();
    }
    return storageIdDTO.map(ResponseEntity::ok)
        .get();
  }
}
