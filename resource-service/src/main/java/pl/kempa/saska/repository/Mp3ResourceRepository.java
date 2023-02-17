package pl.kempa.saska.repository;

import org.springframework.data.repository.CrudRepository;

import pl.kempa.saska.repository.model.Mp3ResourceInfo;

public interface Mp3ResourceRepository extends CrudRepository<Mp3ResourceInfo, Integer> {
  Mp3ResourceInfo findByResourceId(Integer resourceId);
}
