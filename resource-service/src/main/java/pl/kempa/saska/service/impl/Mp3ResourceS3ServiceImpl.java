package pl.kempa.saska.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import pl.kempa.saska.converter.Mp3ResourceConverter;
import pl.kempa.saska.dto.Mp3ResourceDTO;
import pl.kempa.saska.dto.Mp3ResourceDetailsDTO;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.exception.IOServiceException;
import pl.kempa.saska.service.Mp3ResourceS3Service;

@Service
public class Mp3ResourceS3ServiceImpl implements Mp3ResourceS3Service {

  private static final String DOWNLOAD_PATH =
      "C:\\PERSONAL\\Microservices Fundamentals " + "#5\\Downloads\\";

  private Logger logger = LoggerFactory.getLogger(Mp3ResourceS3ServiceImpl.class);

  @Autowired private AmazonS3 s3Client;
  @Autowired private Mp3ResourceConverter converter;

  @Value("${aws.s3.bucket}") private String bucketName;

  @Override public List<Mp3ResourceInfoDTO> getAll() {
    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);
    ObjectListing listObjects = s3Client.listObjects(listObjectsRequest);
    return listObjects.getObjectSummaries()
        .stream()
        .map(converter::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Mp3ResourceDTO download(Mp3ResourceDetailsDTO fileDetails, List<HttpRange> ranges) {
    String fileName = fileDetails.getFileName();
    GetObjectRequest request = new GetObjectRequest(bucketName, fileName);
    if (!ranges.isEmpty()) {
      HttpRange range = ranges.stream()
          .findFirst()
          .get();
      request = request.withRange(range.getRangeStart(fileDetails.getFileSize()),
          range.getRangeEnd(fileDetails.getFileSize()));
    }
    S3Object s3object = s3Client.getObject(request);
    InputStream inputStream = s3object.getObjectContent();
    File mp3Resource = new File(DOWNLOAD_PATH + fileName);
    try {
      Files.copy(inputStream, mp3Resource.toPath(), StandardCopyOption.REPLACE_EXISTING);
      s3object.close();
      IOUtils.closeQuietly(inputStream);
      return new Mp3ResourceDTO(mp3Resource.getAbsolutePath()
          .replaceAll("\\\\", "/"));
    } catch (IOException e) {
      logger.error("IOException: " + e.getMessage());
      throw new IOServiceException(e.getMessage(), e);
    }
  }

  @Override public Optional<String> upload(MultipartFile mp3Resource) {
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(mp3Resource.getSize());
      String keyName = mp3Resource.getOriginalFilename();
      PutObjectResult putObjectResult =
          s3Client.putObject(bucketName, keyName, mp3Resource.getInputStream(), metadata);
      return Optional.ofNullable(putObjectResult.getETag());
    } catch (IOException e) {
      logger.error("IOException: " + e.getMessage());
      throw new IOServiceException(e.getMessage(), e);
    }
  }

  @Override public void delete(String fileName) {
    s3Client.deleteObject(this.bucketName, fileName);
  }
}
