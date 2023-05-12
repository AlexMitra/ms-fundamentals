package pl.kempa.saska.service.impl;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pl.kempa.saska.dto.StorageType.STAGING;
import static pl.kempa.saska.dto.StorageType.TEST;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import pl.kempa.saska.converter.Mp3ResourceConverter;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.service.Mp3ResourceDBService;
import pl.kempa.saska.service.util.ResourceIdGenerator;
import pl.kempa.saska.rest.exception.Mp3DetailsNotFoundException;

@ExtendWith(MockitoExtension.class)
class Mp3ResourceS3ServiceImplUnitTest {

  @Mock private AmazonS3 s3Client;
  @Mock private Mp3ResourceDBService mp3ResourceDBService;
  @Spy private Mp3ResourceConverter converter;
  @Mock private ResourceIdGenerator generator;

  @Mock private ObjectListing listObjects;
  @Mock private ListObjectsRequest listObjectsRequest;
  @Mock private S3ObjectSummary s3ObjectSummary;
  @Mock private Date lastModified;
  @Mock private GetObjectRequest getObjectRequest;
  @Mock private S3Object s3Object;
  @Mock private S3ObjectInputStream inputStream;
  @Mock private InputStream fileStream;
  @Mock private MultipartFile mp3Resource;
  @Mock private PutObjectResult putObjectResult;

  @InjectMocks private Mp3ResourceS3ServiceImpl s3Service;

  @Test
  void getAll_filesPresent_dtoList() {
    // given
    given(s3Client.listObjects(listObjectsRequest)).willReturn(listObjects);
    given(s3ObjectSummary.getKey()).willReturn("key");
    given(s3ObjectSummary.getSize()).willReturn(1000l);
    given(s3ObjectSummary.getLastModified()).willReturn(lastModified);
    given(lastModified.getTime()).willReturn(1672570800000l);
    given(listObjects.getObjectSummaries()).willReturn(List.of(s3ObjectSummary));

    // when
    var dtoList = s3Service.getAll(listObjectsRequest);

    // then
    assertThat(dtoList.size(), equalTo(1));
  }

  @Test
  void getAll_emptyBucket_emptyList() {
    // given
    given(s3Client.listObjects(listObjectsRequest)).willReturn(listObjects);
    given(listObjects.getObjectSummaries()).willReturn(emptyList());

    // when
    var dtoList = s3Service.getAll(listObjectsRequest);

    // then
    assertTrue(dtoList.isEmpty());
  }

  @Test
  void download_noRangesAndFileIsPresent_InputStream() {
    // given
    given(s3Client.getObject(getObjectRequest)).willReturn(s3Object);
    given(s3Object.getObjectContent()).willReturn(inputStream);

    // when
    InputStream stream = s3Service.download(getObjectRequest, emptyList());

    // then
    assertNotNull(stream);
  }

  @Test
  void download_noRangesAndFileIsNotPresent_exceptionThatFileIsNotExist() {
    // given
    String exceptionMessage = "The specified key does not exist.";
    Mp3DetailsNotFoundException exception = new Mp3DetailsNotFoundException(exceptionMessage);
    given(s3Client.getObject(getObjectRequest)).willThrow(exception);

    // when
    Mp3DetailsNotFoundException thrown = assertThrows(
        Mp3DetailsNotFoundException.class,
        () -> s3Service.download(getObjectRequest, emptyList())
    );

    // then
    assertTrue(thrown.getMessage()
        .contentEquals(exceptionMessage));
  }

  @Test
  void upload_fileUploaded_mp3ResourceIdDTO()
      throws IOException {
    // given
    Integer resourceId = 123456789;
    given(mp3Resource.getSize()).willReturn(1000l);
    given(mp3Resource.getOriginalFilename()).willReturn("test_file.mp3");
    given(generator.generateId(any(String.class))).willReturn(resourceId);
    given(mp3Resource.getInputStream()).willReturn(fileStream);
    given(s3Client.putObject(any(String.class), any(String.class), any(InputStream.class),
        any(ObjectMetadata.class))).willReturn(putObjectResult);
    given(putObjectResult.getETag()).willReturn("test_eTag");
    StorageDTO testStorage = StorageDTO.builder()
        .id(1)
        .storageType(TEST)
        .bucket("a-lautsou" +
            "-resources-storage" +
            "-test-1")
        .path("files/")
        .build();

    // when
    Optional<Mp3ResourceIdDTO> resourceIdDTO = s3Service.upload(mp3Resource, testStorage);

    // then
    assertTrue(resourceIdDTO.isPresent());
    assertThat(resourceIdDTO.get()
        .getId(), equalTo(resourceId));
  }

  @Test
  void delete_fileIsPresentAndDeletedAfter_deleteObjectIsCalledOnce() {
    // given
    String fileName = "123456789";
    String bucketName = "test_bucket_name";
    doNothing().when(s3Client)
        .deleteObject(any(String.class), any(String.class));

    // when
    s3Service.delete(fileName, bucketName);

    // then
    verify(s3Client, times(1)).deleteObject(bucketName, fileName);
  }
}