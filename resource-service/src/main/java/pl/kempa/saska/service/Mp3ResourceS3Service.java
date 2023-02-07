package pl.kempa.saska.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpRange;
import org.springframework.web.multipart.MultipartFile;

import pl.kempa.saska.dto.Mp3ResourceDTO;
import pl.kempa.saska.dto.Mp3ResourceDetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;

public interface Mp3ResourceS3Service {
	List<Mp3ResourceInfoDTO> getAll();

	Mp3ResourceDTO download(Mp3ResourceDetailsDTO fileDetails, List<HttpRange> ranges);

	Optional<String> upload(MultipartFile mp3Resource);

	void delete(String fileName);
}
