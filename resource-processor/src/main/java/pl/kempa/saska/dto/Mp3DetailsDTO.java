package pl.kempa.saska.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mp3DetailsDTO {
    private Integer resourceId;
    private String title;
    private String artist;
    private String album;
    private String length;
    private String releaseDate;
}
