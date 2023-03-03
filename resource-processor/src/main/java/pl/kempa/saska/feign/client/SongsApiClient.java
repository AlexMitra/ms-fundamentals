package pl.kempa.saska.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.kempa.saska.dto.Mp3DetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;

@FeignClient(value = "song-service-api", url = "http://localhost:8082/api/songs")
public interface SongsApiClient {

  @RequestMapping(method = RequestMethod.POST)
  Mp3ResourceIdDTO saveMp3Details(@RequestBody Mp3DetailsDTO mp3DetailsDTO);
}
