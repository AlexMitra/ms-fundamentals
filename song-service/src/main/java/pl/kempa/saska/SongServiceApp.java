package pl.kempa.saska;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class SongServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(SongServiceApp.class, args);
    }

}
