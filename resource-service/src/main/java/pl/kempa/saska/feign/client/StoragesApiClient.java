package pl.kempa.saska.feign.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pl.kempa.saska.dto.StorageDTO;

@FeignClient(value = "storage-service/api/storages")
public interface StoragesApiClient {
  @RequestMapping(method = RequestMethod.GET)
  List<StorageDTO> getStoragesByType(@RequestParam String storageType);

  @RequestMapping(method = RequestMethod.GET, value = "/{id}")
  StorageDTO getStoragesById(@PathVariable Integer id);
}
