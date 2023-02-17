package pl.kempa.saska.listener;

import pl.kempa.saska.dto.Mp3ResourceIdDTO;

public interface RabbitMQService {
  void mp3ResourceUploadMessageSend(Mp3ResourceIdDTO resourceIdDTO);
}
