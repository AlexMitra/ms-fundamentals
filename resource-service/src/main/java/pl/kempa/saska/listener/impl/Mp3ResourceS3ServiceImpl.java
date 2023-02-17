package pl.kempa.saska.listener.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.converter.Mp3ResourceConverter;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.dto.Mp3ResourceS3InfoDTO;
import pl.kempa.saska.exception.IOServiceException;
import pl.kempa.saska.listener.Mp3ResourceDBService;
import pl.kempa.saska.listener.Mp3ResourceS3Service;
import pl.kempa.saska.listener.util.ResourceIdGenerator;

@Service
@Slf4j
public class Mp3ResourceS3ServiceImpl implements Mp3ResourceS3Service {

  private static final String DOWNLOAD_PATH =
      "C:\\PERSONAL\\Microservices Fundamentals " + "#5\\Downloads\\";

  @Autowired
  private AmazonS3 s3Client;

  @Autowired Mp3ResourceDBService mp3ResourceDBService;
  @Autowired
  private Mp3ResourceConverter converter;
  @Autowired
  private ResourceIdGenerator generator;
  @Value("${aws.s3.bucket}") private String bucketName;

  @Override
  public List<Mp3ResourceS3InfoDTO> getAll() {
    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);
    ObjectListing listObjects = s3Client.listObjects(listObjectsRequest);
    return listObjects.getObjectSummaries()
        .stream()
        .map(converter::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public InputStream download(Integer resourceId, List<HttpRange> ranges) {
    String key = resourceId.toString();
    GetObjectRequest request = new GetObjectRequest(bucketName, key);
    if (!ranges.isEmpty()) {
      HttpRange range = ranges.stream()
          .findFirst()
          .get();
      mp3ResourceDBService.getByResourceId(resourceId)
          .map(Mp3ResourceInfoDTO::getFileSize)
          .ifPresent(size -> {
            request.withRange(range.getRangeStart(size),
                range.getRangeEnd(size));
          });
    }
    S3Object s3object = s3Client.getObject(request);
    return s3object.getObjectContent();
  }

  @Override
  public Optional<Mp3ResourceIdDTO> upload(MultipartFile mp3Resource) {
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(mp3Resource.getSize());
      Integer resourceId = generator.generateId(mp3Resource.getOriginalFilename());
      PutObjectResult putObjectResult =
          s3Client.putObject(bucketName, resourceId.toString(), mp3Resource.getInputStream(),
              metadata);
      return Optional.ofNullable(putObjectResult.getETag())
          .map(etag -> resourceId)
          .map(Mp3ResourceIdDTO::new);
    } catch (IOException e) {
      throw new IOServiceException(e.getMessage(), e);
    }
  }

  @Override
  public void delete(String fileName) {
    s3Client.deleteObject(this.bucketName, fileName);
  }
}
