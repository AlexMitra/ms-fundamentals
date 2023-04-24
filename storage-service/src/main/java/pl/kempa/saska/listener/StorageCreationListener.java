package pl.kempa.saska.listener;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.dto.StorageIdDTO;
import pl.kempa.saska.dto.StorageType;
import pl.kempa.saska.service.StorageService;

@Component
@Slf4j
public class StorageCreationListener implements ApplicationListener<ApplicationReadyEvent> {

  @Value("${app.staging-bucket}") private String storageStaging;
  @Value("${app.permanent-bucket}") private String storagePermanent;
  @Autowired private StorageService service;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    getDefaultStorages().stream()
        .map(service::create)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(StorageIdDTO::getId)
        .forEach(id -> log.info("!!! Storage with id={} was created !!!", id));
  }

  private List<StorageDTO> getDefaultStorages() {
    StorageDTO staging = StorageDTO.builder()
        .bucket(storageStaging)
        .storageType(StorageType.STAGING)
        .path("files/")
        .build();
    StorageDTO permanent = StorageDTO.builder()
        .bucket(storagePermanent)
        .storageType(StorageType.PERMANENT)
        .path("files/")
        .build();
    return List.of(staging, permanent);
  }
}
