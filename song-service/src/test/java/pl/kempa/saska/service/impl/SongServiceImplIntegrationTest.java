package pl.kempa.saska.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.junit.jupiter.Testcontainers;

import pl.kempa.saska.SongServiceApp;
import pl.kempa.saska.dto.SongInfoDTO;
import pl.kempa.saska.service.SongService;

@SpringBootTest(classes = SongServiceApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = SongServiceIntegrationTestConfig.class)
@Testcontainers
@ActiveProfiles("integrationtest")
class SongServiceImplIntegrationTest {

  @Autowired private SongService songService;

  @Test
  void getAll_twoSongsAreSavedBefore_fetchedSavedSongs() {
    // given
    var songInfoDTO1 = SongInfoDTO.builder()
        .resourceId(111)
        .title("test-title-1")
        .artist("test-artist-1")
        .album("test-album-1")
        .length("0:15")
        .releaseDate("2023-03-01")
        .build();

    var songInfoDTO2 = SongInfoDTO.builder()
        .resourceId(222)
        .title("test-title-2")
        .artist("test-artist-2")
        .album("test-album-2")
        .length("0:16")
        .releaseDate("2023-03-01")
        .build();

    // when
    songService.save(songInfoDTO1);
    songService.save(songInfoDTO2);
    var songs = songService.getAll();

    // then
    assertThat(songs, hasSize(2));
  }
}