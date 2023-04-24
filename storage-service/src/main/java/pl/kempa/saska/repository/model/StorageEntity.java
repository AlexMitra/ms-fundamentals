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
@Table(name = "tblstorage")
public class StorageEntity {
  @Id
  @GeneratedValue
  private Integer id;

  @Column(name = "storagetype")
  private String storageType;

  @Column(name = "bucketname")
  private String bucketName;

  @Column(name = "path")
  private String path;
}
