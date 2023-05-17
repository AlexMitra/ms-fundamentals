package pl.kempa.saska.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/gateway")
public class HelloController {

  @GetMapping
  public String hello() {
    return "Hello from api-gateway";
  }
}
