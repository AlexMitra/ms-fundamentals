package pl.kempa.saska.listener;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpRange;
import org.springframework.web.multipart.MultipartFile;

import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceS3InfoDTO;

public interface Mp3ResourceS3Service {
  List<Mp3ResourceS3InfoDTO> getAll();

	InputStream download(Integer resourceId, List<HttpRange> ranges);

  Optional<Mp3ResourceIdDTO> upload(MultipartFile mp3Resource);

  void delete(String fileName);
}
