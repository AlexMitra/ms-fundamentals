package pl.kempa.saska.listener;

import java.util.List;
import java.util.Optional;

import pl.kempa.saska.dto.SongIdDTO;
import pl.kempa.saska.dto.SongInfoDTO;

public interface SongService {
    List<SongInfoDTO> getAll();

    Optional<SongInfoDTO> getById(Integer id);

    SongIdDTO save(SongInfoDTO songInfoDTO);

    Optional<SongInfoDTO> delete(Integer id);
}
