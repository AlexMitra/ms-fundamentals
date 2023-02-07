package pl.kempa.saska.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SongIdListDTO {
    private List<Integer> ids;
}
