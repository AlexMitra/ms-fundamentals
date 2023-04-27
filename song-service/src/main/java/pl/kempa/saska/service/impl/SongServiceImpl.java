package pl.kempa.saska.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.kempa.saska.converter.SongConverter;
import pl.kempa.saska.dto.SongIdDTO;
import pl.kempa.saska.dto.SongInfoDTO;
import pl.kempa.saska.repository.SongRepository;
import pl.kempa.saska.repository.model.SongInfo;
import pl.kempa.saska.service.SongService;

@Service
public class SongServiceImpl implements SongService {

  @Autowired private SongRepository repository;
  @Autowired private SongConverter converter;

  @Override
  public List<SongInfoDTO> getAll() {
    Iterable<SongInfo> songs = repository.findAll();
    return StreamSupport.stream(songs.spliterator(), false)
        .map(converter::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SongInfoDTO> getByResourceId(Integer resourceId) {
    return repository.findByResourceId(resourceId)
        .map(converter::toDTO);
  }

  @Override
  public Optional<SongIdDTO> save(SongInfoDTO songInfoDTO) {
    repository.save(converter.toEntity(songInfoDTO));
    return repository.findByResourceId(songInfoDTO.getResourceId()).map(SongInfo::getResourceId).map(SongIdDTO::new);
  }

  @Override
  public Optional<SongInfoDTO> deleteByResourceId(Integer resourceId) {
    Optional<SongInfo> song = repository.findByResourceId(resourceId);
    song.ifPresent(repository::delete);
    return song.map(converter::toDTO);
  }
}
