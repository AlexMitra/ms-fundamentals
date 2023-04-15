package pl.kempa.saska.rest;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.SongIdDTO;
import pl.kempa.saska.dto.SongInfoDTO;
import pl.kempa.saska.service.SongService;

@RestController
@RequestMapping(value = "/api/songs")
@Slf4j
public class SongController {

  @Autowired private SongService songService;

  @Autowired
  public SongController(SongService songService) {
    this.songService = songService;
  }

  @GetMapping(value = "/{resourceId}")
  public ResponseEntity<?> getByResourceId(@PathVariable Integer resourceId) {
    Optional<SongInfoDTO> songInfoDTO = songService.getByResourceId(resourceId);
    if (songInfoDTO.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return ResponseEntity.ok(songInfoDTO);
  }

  @PostMapping
  public ResponseEntity<SongIdDTO> save(@RequestBody SongInfoDTO songInfoDTO) {
    SongIdDTO dto = songService.save(songInfoDTO);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity<?> delete(@PathVariable Integer id) {
    Optional<SongInfoDTO> song = songService.delete(id);
    if (song.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return song.map(ResponseEntity::ok)
        .get();
  }
}
