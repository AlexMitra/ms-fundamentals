package pl.kempa.saska.rest;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.ListObjectsRequest;

import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceS3InfoDTO;
import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.rest.util.WebClientUtil;
import pl.kempa.saska.rest.validator.Mp3ResourceValidator;
import pl.kempa.saska.service.Mp3ResourceDBService;
import pl.kempa.saska.service.Mp3ResourceS3Service;
import pl.kempa.saska.service.RabbitMQService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(Mp3ResourceController.class)
class Mp3ResourceControllerUnitTest {

  @MockBean private Mp3ResourceS3Service s3Service;
  @MockBean private Mp3ResourceDBService mp3ResourceDBService;
  @MockBean private Mp3ResourceValidator validator;
  @MockBean private WebClientUtil webClientUtil;
  @MockBean private RabbitMQService rabbitMQService;
  @MockBean private HttpHeaders httpHeaders;
  @MockBean private MultipartFile mp3file;
  @Autowired private MockMvc mockMvc;

  @Test
  void getAll_oneFilePresent_returnedResourceS3InfoDTOList()
      throws Exception {
    // given
    var lastModified = LocalDateTime.of(2023, 3, 1, 14, 0);
    var resourceS3InfoDTOs = List.of(new Mp3ResourceS3InfoDTO("name", 1000l, lastModified));
    given(s3Service.getAll(any(ListObjectsRequest.class))).willReturn(resourceS3InfoDTOs);

    // when
    // then
    this.mockMvc.perform(MockMvcRequestBuilders
            .get("/api/resources")
            .accept(MediaType.APPLICATION_JSON_UTF8))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(resourceS3InfoDTOs.size())))
        .andExpect(jsonPath("$.[0].name", equalTo(resourceS3InfoDTOs.get(0)
            .getName())))
        .andExpect(jsonPath("$.[0].size", equalTo(resourceS3InfoDTOs.get(0)
            .getSize()
            .intValue())))
        .andExpect(jsonPath("$.[0].lastModified",
            equalTo(lastModified.format(DateTimeFormatter.ISO_DATE_TIME))));
  }

  @Test
  void downloadById_fileIsPresent_returnedFileBytes()
      throws Exception {
    // given
    InputStream fileStream = this.getClass()
        .getClassLoader()
        .getResourceAsStream("sample-15s.mp3");
    URL fileURL = getClass().getClassLoader()
        .getResource("sample-15s.mp3");
    int fileBytes = (int) Files.size(Path.of(fileURL.toURI()));
    given(httpHeaders.getRange()).willReturn(emptyList());
    given(s3Service.download(any(), any())).willReturn(fileStream);

    // when
    var response = this.mockMvc.perform(MockMvcRequestBuilders
            .get("/api/resources/1")
            .accept(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(status().isOk())
        .andReturn();
    var responseLength = response.getResponse()
        .getContentAsByteArray().length;

    // then
    assertThat(responseLength, equalTo(fileBytes));
  }

  @Test
  void upload_fileUploaded_mp3ResourceIdDTO()
      throws Exception {
    // given
    byte[] fileBytes = this.getClass()
        .getClassLoader()
        .getResourceAsStream("sample-15s.mp3")
        .readAllBytes();
    given(validator.validate(mp3file)).willReturn(Optional.empty());
    var resourceIdDTO = Optional.of(new Mp3ResourceIdDTO(1111));
    given(s3Service.upload(any(MultipartFile.class), any(StorageDTO.class))).willReturn(resourceIdDTO);
    given(mp3file.getSize()).willReturn((long) fileBytes.length);

    // when
    // then
    this.mockMvc.perform(MockMvcRequestBuilders
            .multipart("/api/resources")
            .file("file", fileBytes))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo(resourceIdDTO.get()
            .getId())));
  }
}