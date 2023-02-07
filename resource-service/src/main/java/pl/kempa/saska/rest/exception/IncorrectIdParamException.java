package pl.kempa.saska.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IncorrectIdParamException extends RuntimeException {
  public IncorrectIdParamException(String message) {
    super(message);
  }
}
