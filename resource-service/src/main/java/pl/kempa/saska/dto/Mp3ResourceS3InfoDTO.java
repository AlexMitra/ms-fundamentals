package pl.kempa.saska.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mp3ResourceS3InfoDTO {
	private String name;
	private Long size;
	private LocalDateTime lastModified;
}
