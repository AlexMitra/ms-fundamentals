package pl.kempa.saska.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageDTO {
  Integer id;
  StorageType storageType;
  String bucket;
  String path;
}
