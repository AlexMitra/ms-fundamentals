package pl.kempa.saska.bdd.stepDefinitions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashMap;

import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pl.kempa.saska.dto.SongInfoDTO;

public class SongControllerSaveSongInfoE2ESteps {

  @LocalServerPort private int port;

  private final Integer songResourceId = 111;
  private SongInfoDTO songInfoDTO;
  private ResponseEntity<LinkedHashMap> response;

  private HttpEntity<?> getSongDTO()
      throws JsonProcessingException {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    songInfoDTO = SongInfoDTO.builder()
        .resourceId(songResourceId)
        .title("test-title")
        .artist("test-artist")
        .album("test-album")
        .length("0:15")
        .releaseDate("2023-03-01")
        .build();
    ObjectWriter ow = new ObjectMapper().writer()
        .withDefaultPrettyPrinter();
    var songJSONString = ow.writeValueAsString(songInfoDTO);
    return new HttpEntity<>(songJSONString, headers);
  }

  @When("^the client calls /api/songs with SongInfoDTO in body$")
  public void whenClientSavesSongInfoDTO()
      throws JsonProcessingException {
    String url = "http://localhost:" + port + "/api/songs";
    response = new RestTemplate().exchange(url, HttpMethod.POST, getSongDTO(), LinkedHashMap.class);
  }

  @Then("^the client receives status code of 200$")
  public void thenStatusIsOk() {
    assertThat(response.getStatusCodeValue(), equalTo(200));
  }

  @And("^the client receives id of saved SongInfoDTO$")
  public void andReceivesSongResourceId() {
    var body = response.getBody();
    assertNotNull(body);
    assertThat(body.get("id"), equalTo(songResourceId));
  }

  @Then("^the client is able to call GET /api/songs/resourceId with same id$")
  public void thenClientGetSongInfoDTOById() {
    String url = "http://localhost:" + port + "/api/songs/" + songResourceId;
    response = new RestTemplate().exchange(url, HttpMethod.GET, null, LinkedHashMap.class);
  }

  @And("^the client receives saved SongInfoDTO$")
  public void andReceivesSavedSongInfoDTO() {
    assertThat(response.getStatusCodeValue(), equalTo(200));
    var body = response.getBody();
    assertNotNull(body);
    assertThat(body.get("resourceId"), equalTo(songInfoDTO.getResourceId()));
    assertThat(body.get("title"), equalTo(songInfoDTO.getTitle()));
    assertThat(body.get("artist"), equalTo(songInfoDTO.getArtist()));
    assertThat(body.get("album"), equalTo(songInfoDTO.getAlbum()));
    assertThat(body.get("length"), equalTo(songInfoDTO.getLength()));
    assertThat(body.get("releaseDate"), equalTo(songInfoDTO.getReleaseDate()));
  }
}
