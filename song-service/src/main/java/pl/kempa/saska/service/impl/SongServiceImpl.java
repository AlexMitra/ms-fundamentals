package pl.kempa.saska.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

  @Autowired
  private SongRepository repository;

  @Autowired
  SongConverter converter;

  @Override
  public List<SongInfoDTO> getAll() {
    Iterable<SongInfo> songs = repository.findAll();
    return StreamSupport.stream(songs.spliterator(), false)
        .map(converter::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SongInfoDTO> getById(Integer id) {
    return repository.findById(id)
        .map(converter::toDTO);
  }

  @Override
  public Optional<SongInfoDTO> getByFileName(String fileName) {
    var songInfoDTO = repository.findByFileName(fileName);
    return Optional.ofNullable(songInfoDTO)
        .map(converter::toDTO);
  }

  @Override
  public boolean isExists(String fileName) {
    return repository.existsByFileName(fileName);
  }

  @Override
  public SongIdDTO save(SongInfoDTO songInfoDTO) {
    repository.save(converter.toEntity(songInfoDTO));
    SongInfo song = repository.findByTitle(songInfoDTO.getTitle());
    return new SongIdDTO(song.getId());
  }

  @Override
  public Optional<SongInfoDTO> delete(Integer id) {
    Optional<SongInfo> song = repository.findById(id);
    song.ifPresent(repository::delete);
    return song.map(converter::toDTO);
  }
}
