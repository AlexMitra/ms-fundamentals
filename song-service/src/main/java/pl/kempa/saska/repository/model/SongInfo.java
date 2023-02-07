package pl.kempa.saska.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tblsonginfo")
public class SongInfo {
  @Id
  @GeneratedValue
  private Integer id;
  @Column(name = "filename")
  private String fileName;
  @Column(name = "filesize")
  private long fileSize;
  private String title;
  private String artist;
  private String album;
  private String length;
  @Column(name = "releasedate")
  private String releaseDate;
}
