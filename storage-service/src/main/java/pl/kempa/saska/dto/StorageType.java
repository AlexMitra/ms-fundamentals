package pl.kempa.saska.dto;

import pl.kempa.saska.rest.exception.NoSuchStorageTypeException;

public enum StorageType {
  // it's possible to create only one storage with PERMANENT or STAGING type
  // no restriction for DUMMY and TEST
  PERMANENT, STAGING, DUMMY, TEST;

  public static StorageType valueOfStorage(String type) {
    try {
      return valueOf(type);
    } catch (IllegalArgumentException e) {
      throw new NoSuchStorageTypeException(String.format("Storage type %s is not supported", type), e);
    }
  }
}
