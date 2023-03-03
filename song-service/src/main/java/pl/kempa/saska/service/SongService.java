package pl.kempa.saska.service;

import java.util.List;
import java.util.Optional;

import pl.kempa.saska.dto.SongIdDTO;
import pl.kempa.saska.dto.SongInfoDTO;

public interface SongService {
    List<SongInfoDTO> getAll();

    Optional<SongInfoDTO> getByResourceId(Integer resourceId);

    SongIdDTO save(SongInfoDTO songInfoDTO);

    Optional<SongInfoDTO> delete(Integer id);
}
