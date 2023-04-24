package pl.kempa.saska.service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpRange;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;

import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceS3InfoDTO;
import pl.kempa.saska.dto.StorageDTO;

public interface Mp3ResourceS3Service {
  List<Mp3ResourceS3InfoDTO> getAll(ListObjectsRequest listObjectsRequest);

  InputStream download(GetObjectRequest getObjectRequest, List<HttpRange> ranges);

  Optional<Mp3ResourceIdDTO> upload(MultipartFile mp3Resource, StorageDTO storageDTO);

  void delete(String fileName, String bucketName);
}
