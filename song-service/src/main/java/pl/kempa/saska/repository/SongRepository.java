package pl.kempa.saska.repository;

import org.springframework.data.repository.CrudRepository;

import pl.kempa.saska.repository.model.SongInfo;

public interface SongRepository extends CrudRepository<SongInfo, Integer> {
  SongInfo findByResourceId(Integer resourceId);
}
