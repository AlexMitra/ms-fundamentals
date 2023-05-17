package pl.kempa.saska.listener;

import static pl.kempa.saska.dto.StorageType.PERMANENT;

import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.Mp3ResourceIdDTO;
import pl.kempa.saska.dto.Mp3ResourceInfoDTO;
import pl.kempa.saska.dto.StorageDTO;
import pl.kempa.saska.rest.util.WebClientUtil;
import pl.kempa.saska.service.Mp3ResourceDBService;

@Component
@Slf4j
public class Mp3ResourceProcessedListener {

  private Mp3ResourceDBService mp3ResourceDBService;
  private WebClientUtil webClientUtil;
  private AmazonS3 s3Client;
  private Counter filesDeletedFromStaging;
  private Counter filesUploadedToPermanent;

  public Mp3ResourceProcessedListener(Mp3ResourceDBService mp3ResourceDBService,
                                      WebClientUtil webClientUtil, AmazonS3 s3Client,
                                      MeterRegistry meterRegistry) {
    this.mp3ResourceDBService = mp3ResourceDBService;
    this.webClientUtil = webClientUtil;
    this.s3Client = s3Client;
    this.filesDeletedFromStaging = Counter
        .builder("deleted.mp3.from.staging.counter")
        .description("count number of files deleted from STAGING storage")
        .register(meterRegistry);
    this.filesUploadedToPermanent = Counter
        .builder("upload.mp3.to.permanent.counter")
        .description("count number of files uploaded to PERMANENT storage")
        .register(meterRegistry);
  }

  @Timed("move.mp3.to.permanent")
  @RabbitListener(queues = "${spring.rabbitmq.queue.resource-processed}")
  public void onMp3ResourceProcessed(Mp3ResourceIdDTO resourceIdDTO) {
    // 1) get Info about current and PERMANENT storages
    var resourceInfoOpt = mp3ResourceDBService.getByResourceId(resourceIdDTO.getId());
    var currentStorage = resourceInfoOpt.map(Mp3ResourceInfoDTO::getStorageId)
        .map(webClientUtil::callGetStoragesById);
    var permanentStorage = webClientUtil.callGetStoragesByType(PERMANENT)
        .stream()
        .findFirst();
    if (currentStorage.isEmpty() || permanentStorage.isEmpty()) {
      log.error("Either {} or {} storage doesn't exist", currentStorage, permanentStorage);
      return;
    }
    // 2) move file to PERMANENT storage
    var resourceStr = resourceIdDTO.getId()
        .toString();
    copyResourceToStorage(currentStorage.get(), permanentStorage.get(), resourceStr).ifPresent(
        r -> {
          filesUploadedToPermanent.increment();
          deleteResourceFromStorage(currentStorage.get(), resourceStr);
        });
    // 3) change storage id in DB
    var resourceInfoDTO = resourceInfoOpt.get();
    permanentStorage.map(StorageDTO::getId)
        .ifPresent(sId -> {
          resourceInfoDTO.setStorageId(sId);
          mp3ResourceDBService.update(resourceInfoDTO);
          log.info("Storage was changed to PERMANENT {}", permanentStorage.get()
              .getBucket());
        });
  }

  private Optional<String> copyResourceToStorage(StorageDTO current, StorageDTO target,
                                                 String resourceId) {
    CopyObjectRequest copyRequest =
        new CopyObjectRequest().withSourceBucketName(current.getBucket())
            .withSourceKey(current.getPath()
                .concat(resourceId))
            .withDestinationBucketName(target.getBucket())
            .withDestinationKey(target.getPath()
                .concat(resourceId));

    try {
      CopyObjectResult result = s3Client.copyObject(copyRequest);
      return Optional.of(result.toString());
    } catch (AmazonServiceException e) {
      log.error("Can't copy resource to {} bucket: {}", target.getBucket(), e.getErrorMessage());
    }
    return Optional.empty();
  }

  private void deleteResourceFromStorage(StorageDTO target, String resourceId) {
    s3Client.deleteObject(target.getBucket(), target.getPath().concat(resourceId));
    filesDeletedFromStaging.increment();
  }
}
