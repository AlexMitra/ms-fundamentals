package pl.kempa.saska.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StorageDTO {
  Integer id;
  StorageType storageType;
  String bucket;
  String path;
}
