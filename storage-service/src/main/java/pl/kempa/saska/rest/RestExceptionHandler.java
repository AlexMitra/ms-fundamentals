package pl.kempa.saska.rest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import pl.kempa.saska.dto.ApiErrorDTO;
import pl.kempa.saska.rest.exception.NoSuchStorageTypeException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler {
  @ExceptionHandler({NoSuchStorageTypeException.class})
  public ResponseEntity<ApiErrorDTO> handleNoSuchStorageTypeException(NoSuchStorageTypeException e) {
    var errorDTO = new ApiErrorDTO(HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.badRequest()
        .body(errorDTO);
  }
}
