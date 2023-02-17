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
@Table(name = "tblmp3resourceinfo")
public class Mp3ResourceInfo {
  @Id
  @GeneratedValue
  private Integer id;

  @Column(name = "resourceid")
  private Integer resourceId;

  @Column(name = "filesize")
  private Long fileSize;
}
