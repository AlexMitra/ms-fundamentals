package pl.kempa.saska.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "song-service/api/songs")
public interface SongsApiClient {
  @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
  void deleteMp3Details(@PathVariable Integer id);
}
