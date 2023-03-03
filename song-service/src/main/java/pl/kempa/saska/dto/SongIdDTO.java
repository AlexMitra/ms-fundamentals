package pl.kempa.saska.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SongIdDTO implements Serializable {
  private Integer Id;
}
