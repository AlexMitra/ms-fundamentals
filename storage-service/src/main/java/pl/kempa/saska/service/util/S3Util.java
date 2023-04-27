package pl.kempa.saska.service.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.StorageDTO;

@Component
@AllArgsConstructor
@Slf4j
public class S3Util {

  private AmazonS3 s3Client;

  public void createFolder(Bucket bucket, StorageDTO storageDTO) {
    ObjectMetadata metadata = new ObjectMetadata();
    // create empty content
    metadata.setContentLength(0);
    InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
    var path = storageDTO.getPath();
    var folderPath = path.endsWith("/") ? path : path.concat("/");
    PutObjectRequest putObjectRequest =
        new PutObjectRequest(bucket.getName(), folderPath, emptyContent,
            metadata);
    s3Client.putObject(putObjectRequest);
  }

  public void deleteBucketContent(String bucketName) {
    var listObjectsRequest = new ListObjectsRequest()
        .withBucketName(bucketName);
    // get all folders and files in the bucket
    var keys =
        s3Client.listObjects(listObjectsRequest)
            .getObjectSummaries()
            .stream()
            .map(S3ObjectSummary::getKey)
            .toArray(String[]::new);
    // delete them
    var multiObjectDeleteRequest = new DeleteObjectsRequest(bucketName)
        .withKeys(keys)
        .withQuiet(false);
    s3Client.deleteObjects(multiObjectDeleteRequest);
  }
}
