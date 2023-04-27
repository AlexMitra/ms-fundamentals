package pl.kempa.saska.rest.exception;

public class NoSuchStorageTypeException extends RuntimeException {

  public NoSuchStorageTypeException() {
  }

  public NoSuchStorageTypeException(String message) {
    super(message);
  }

  public NoSuchStorageTypeException(String message, Throwable cause) {
    super(message, cause);
  }
}
