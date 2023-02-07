package pl.kempa.saska.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import pl.kempa.saska.dto.SongIdDTO;
import pl.kempa.saska.dto.SongIdListDTO;
import pl.kempa.saska.dto.SongInfoDTO;

public interface SongService {

    List<SongInfoDTO> getAll();
    Optional<SongInfoDTO> getById(Integer id);
    Optional<SongInfoDTO> getByFileName(String fileName);
    boolean isExists(String fileName);
    SongIdDTO save(SongInfoDTO songInfoDTO);

    Optional<SongInfoDTO> delete(Integer id);
}
