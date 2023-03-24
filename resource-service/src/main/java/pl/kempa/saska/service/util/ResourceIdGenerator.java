package pl.kempa.saska.service.util;

import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

@Component
public class ResourceIdGenerator {

  public Integer generateId(String fileName) {
    String sourceComplexString =
        new StringBuilder(fileName).append(System.currentTimeMillis())
            .append(UUID.randomUUID())
            .toString();
    int resourceId = convertStringToId(sourceComplexString);
    return resourceId < 0 ? -1 * resourceId : resourceId;
  }

  private int convertStringToId(String str) {
    int random = new Random().nextInt(777777) + 1;
    return IntStream.range(0, str.length())
        .map(str::charAt)
        .mapToObj(Integer::toHexString)
        .mapToInt(s -> Integer.parseInt(s, 16) * random)
        .sum();
  }
}
