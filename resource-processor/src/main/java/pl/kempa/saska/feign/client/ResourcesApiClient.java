package pl.kempa.saska.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "resource-service-api-client", url = "${feign.client.url.resource-service}")
public interface ResourcesApiClient {

  @RequestMapping(method = RequestMethod.GET, value = "/{resourceId}")
  byte[] getMp3Resource(@PathVariable String resourceId);
}
