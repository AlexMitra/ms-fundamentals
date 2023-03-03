package pl.kempa.saska.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.converter.Mp3ResourceConverter;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.dto.Mp3ResourceS3InfoDTO;
import pl.kempa.saska.exception.IOServiceException;
import pl.kempa.saska.service.Mp3ResourceDBService;
import pl.kempa.saska.service.Mp3ResourceS3Service;
import pl.kempa.saska.service.util.ResourceIdGenerator;
import pl.kempa.saska.rest.exception.Mp3DetailsNotFoundException;

@Service
@AllArgsConstructor
@Slf4j
public class Mp3ResourceS3ServiceImpl implements Mp3ResourceS3Service {

  private AmazonS3 s3Client;
  private Mp3ResourceDBService mp3ResourceDBService;
  private Mp3ResourceConverter converter;
  private ResourceIdGenerator generator;

  @Override
  public List<Mp3ResourceS3InfoDTO> getAll(ListObjectsRequest listObjectsRequest) {
    ObjectListing listObjects = s3Client.listObjects(listObjectsRequest);
    return listObjects.getObjectSummaries()
        .stream()
        .map(converter::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public InputStream download(GetObjectRequest request, List<HttpRange> ranges) {
    if (!ranges.isEmpty()) {
      HttpRange range = ranges.stream()
          .findFirst()
          .get();
      mp3ResourceDBService.getByResourceId(Integer.valueOf(request.getKey()))
          .map(Mp3ResourceInfoDTO::getFileSize)
          .ifPresent(size -> request.withRange(range.getRangeStart(size), range.getRangeEnd(size)));
    }
    try {
      S3Object s3object = s3Client.getObject(request);
      return s3object.getObjectContent();
    } catch (AmazonS3Exception e) {
      throw new Mp3DetailsNotFoundException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<Mp3ResourceIdDTO> upload(MultipartFile mp3Resource, String bucketName) {
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
  public void delete(String fileName, String bucketName) {
    s3Client.deleteObject(bucketName, fileName);
  }
}
