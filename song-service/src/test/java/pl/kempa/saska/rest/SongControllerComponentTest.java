package pl.kempa.saska.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;

import pl.kempa.saska.dto.SongInfoDTO;
import pl.kempa.saska.service.SongService;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"componenttest"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SongControllerComponentTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private SongService songService;

  @Test
  void getById_songIsSavedBefore_fetchedSavedSongByResourceId()
      throws Exception {
    // given
    var songInfoDTO = SongInfoDTO.builder()
        .resourceId(111)
        .title("test-title-1")
        .artist("test-artist-1")
        .album("test-album-1")
        .length("0:15")
        .releaseDate("2023-03-01")
        .build();
    ObjectWriter ow = new ObjectMapper().writer()
        .withDefaultPrettyPrinter();
    String songForSaving = ow.writeValueAsString(songInfoDTO);

    // when
    this.mockMvc.perform(MockMvcRequestBuilders
            .post("/api/songs")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(songForSaving))
        .andDo(print())
        .andExpect(status().isOk());

    // then
    this.mockMvc.perform(MockMvcRequestBuilders
            .get("/api/songs/" + songInfoDTO.getResourceId())
            .accept(MediaType.APPLICATION_JSON_UTF8))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resourceId", equalTo(songInfoDTO.getResourceId())))
        .andExpect(jsonPath("$.title", equalTo(songInfoDTO.getTitle())))
        .andExpect(jsonPath("$.artist", equalTo(songInfoDTO.getArtist())))
        .andExpect(jsonPath("$.album", equalTo(songInfoDTO.getAlbum())))
        .andExpect(jsonPath("$.length", equalTo(songInfoDTO.getLength())))
        .andExpect(jsonPath("$.releaseDate", equalTo(songInfoDTO.getReleaseDate())));
  }
}