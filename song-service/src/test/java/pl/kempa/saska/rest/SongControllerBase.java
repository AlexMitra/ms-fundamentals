package pl.kempa.saska.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import pl.kempa.saska.SongServiceApp;
import pl.kempa.saska.dto.SongIdDTO;
import pl.kempa.saska.dto.SongInfoDTO;
import pl.kempa.saska.service.impl.SongServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SongServiceApp.class)
@ActiveProfiles({"componenttest"})
public abstract class SongControllerBase {

  @Autowired WebApplicationContext webApplicationContext;
  @MockBean SongServiceImpl songService;

  @BeforeEach
  public void setup() {
    Integer resourceId = 111;
    given(songService.save(any(SongInfoDTO.class))).willReturn(Optional.of(new SongIdDTO(resourceId)));
    RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
  }
}