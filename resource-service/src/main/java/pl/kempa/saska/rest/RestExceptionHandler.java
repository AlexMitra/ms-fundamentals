package pl.kempa.saska.rest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import pl.kempa.saska.dto.ApiErrorDTO;
import pl.kempa.saska.exception.IOServiceException;
import pl.kempa.saska.exception.StorageAuthorizationException;
import pl.kempa.saska.rest.exception.IncorrectIdParamException;
import pl.kempa.saska.rest.exception.Mp3DetailsNotFoundException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler({Mp3DetailsNotFoundException.class})
  public ResponseEntity<ApiErrorDTO> handleMp3DetailsNotFoundException(
      Mp3DetailsNotFoundException e) {
    var errorDTO = new ApiErrorDTO(HttpStatus.NOT_FOUND, e.getMessage());
    return new ResponseEntity(errorDTO, new HttpHeaders(), errorDTO.getStatus());
  }

  @ExceptionHandler({IncorrectIdParamException.class})
  public ResponseEntity<ApiErrorDTO> handleIncorrectIdParamException(IncorrectIdParamException e) {
    var errorDTO = new ApiErrorDTO(HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.badRequest()
        .body(errorDTO);
  }

  @ExceptionHandler({AmazonServiceException.class})
  public ResponseEntity<ApiErrorDTO> handleAmazonServiceException(AmazonServiceException e) {
    var errorDTO = new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR,
        e.getStatusCode() + ": " + e.getErrorMessage());
    return new ResponseEntity(errorDTO, new HttpHeaders(), errorDTO.getStatus());
  }

  @ExceptionHandler({AmazonClientException.class})
  public ResponseEntity<ApiErrorDTO> handleAmazonClientException(AmazonClientException e) {
    var errorDTO = new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    return new ResponseEntity(errorDTO, new HttpHeaders(), errorDTO.getStatus());
  }

  @ExceptionHandler({IOServiceException.class})
  public ResponseEntity<ApiErrorDTO> handleIOException(IOServiceException e) {
    var errorDTO = new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    return new ResponseEntity(errorDTO, new HttpHeaders(), errorDTO.getStatus());
  }

  @ExceptionHandler({StorageAuthorizationException.class})
  public ResponseEntity<ApiErrorDTO> handleStorageAuthorizationException(
      StorageAuthorizationException e) {
    var errorDTO = new ApiErrorDTO(HttpStatus.UNAUTHORIZED, e.getMessage());
    return new ResponseEntity(errorDTO, new HttpHeaders(), errorDTO.getStatus());
  }

  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<ApiErrorDTO> handleRuntimeException(RuntimeException e) {
    var errorDTO = new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    return new ResponseEntity(errorDTO, new HttpHeaders(), errorDTO.getStatus());
  }
}
