package pl.kempa.saska;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@EnableEurekaClient
@EnableResourceServer
@SpringBootApplication
public class StorageServiceApp {
  public static void main(String[] args) {
    SpringApplication.run(StorageServiceApp.class, args);
  }
}
