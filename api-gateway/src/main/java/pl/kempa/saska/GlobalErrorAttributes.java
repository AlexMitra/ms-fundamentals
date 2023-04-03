package pl.kempa.saska;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

  @Override
  public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
    Throwable error = super.getError(request);
    Map<String, Object> attributes = super.getErrorAttributes(request, options);
    // just make error response more accurate
    HttpStatus status = getHttpStatus(attributes);
    attributes.put("code",
        status.equals(HttpStatus.NOT_FOUND) ? HttpStatus.BAD_REQUEST.value() : status.value());
    attributes.put("message", status.equals(HttpStatus.INTERNAL_SERVER_ERROR)
        ? "Oops, our server is on a break"
        : error.getMessage());
    attributes.remove("status");
    attributes.remove("timestamp");
    attributes.remove("requestId");
    return attributes;
  }

  private HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
    int statusCode = (int) errorAttributes.get("status");
    return HttpStatus.valueOf(statusCode);
  }
}