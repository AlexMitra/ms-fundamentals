package pl.kempa.saska.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.converter.Mp3ResourceConverter;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.repository.Mp3ResourceRepository;
import pl.kempa.saska.repository.model.Mp3ResourceInfo;
import pl.kempa.saska.service.Mp3ResourceDBService;

@Service
@Slf4j
@AllArgsConstructor
public class Mp3ResourceDBServiceImpl implements Mp3ResourceDBService {

  private Mp3ResourceRepository repository;
  private Mp3ResourceConverter converter;

  @Override
  public List<Mp3ResourceInfoDTO> getAll() {
    Iterable<Mp3ResourceInfo> mp3Resources = repository.findAll();
    return StreamSupport.stream(mp3Resources.spliterator(), false)
        .map(converter::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Mp3ResourceInfoDTO> getByResourceId(Integer resourceId) {
    var mp3ResourceInfo = repository.findByResourceId(resourceId);
    return Optional.ofNullable(mp3ResourceInfo)
        .map(converter::toDTO);
  }

  @Override
  public void save(Mp3ResourceInfoDTO mp3ResourceInfoDTO) {
    repository.save(converter.toEntity(mp3ResourceInfoDTO));
    log.info("Information about resource {} was saved to resource-service DB",
        mp3ResourceInfoDTO.getResourceId());
  }

  @Override
  public void update(Mp3ResourceInfoDTO mp3ResourceInfoDTO) {
    var resourceInfo = repository.findById(mp3ResourceInfoDTO.getId());
    resourceInfo.ifPresent(entity -> {
      entity.setStorageId(mp3ResourceInfoDTO.getStorageId());
      repository.save(entity);
    });
  }

  @Override
  public Optional<Mp3ResourceInfoDTO> delete(Integer resourceId) {
    Optional<Mp3ResourceInfo> mp3ResourceInfo =
        Optional.ofNullable(repository.findByResourceId(resourceId));
    mp3ResourceInfo.ifPresent(repository::delete);
    return mp3ResourceInfo.map(converter::toDTO);
  }
}
