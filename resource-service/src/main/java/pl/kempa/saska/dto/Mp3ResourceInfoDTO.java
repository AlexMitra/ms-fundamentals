package pl.kempa.saska.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mp3ResourceInfoDTO {
  private Integer id;
  private Integer resourceId;
  private Long fileSize;
  private Integer storageId;
}
