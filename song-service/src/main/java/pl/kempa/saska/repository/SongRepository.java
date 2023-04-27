package pl.kempa.saska.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import pl.kempa.saska.repository.model.SongInfo;

public interface SongRepository extends CrudRepository<SongInfo, Integer> {
  Optional<SongInfo> findByResourceId(Integer resourceId);
}
