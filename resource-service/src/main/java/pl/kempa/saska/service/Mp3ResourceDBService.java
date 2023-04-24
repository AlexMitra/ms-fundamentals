package pl.kempa.saska.service;

import java.util.List;
import java.util.Optional;

import pl.kempa.saska.dto.Mp3ResourceInfoDTO;

public interface Mp3ResourceDBService {

  List<Mp3ResourceInfoDTO> getAll();

  Optional<Mp3ResourceInfoDTO> getByResourceId(Integer resourceId);

  void save(Mp3ResourceInfoDTO mp3ResourceInfoDTO);

  void update(Mp3ResourceInfoDTO mp3ResourceInfoDTO);

  Optional<Mp3ResourceInfoDTO> delete(Integer resourceId);
}
