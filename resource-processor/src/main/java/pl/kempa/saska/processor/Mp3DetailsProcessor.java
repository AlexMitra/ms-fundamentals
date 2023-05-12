package pl.kempa.saska.processor;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.dto.Mp3DetailsDTO;

@Component
@Slf4j
public class Mp3DetailsProcessor {
  @Timed("process.mp3.metadata")
  public Mp3DetailsDTO processMp3(InputStream inputStream, Integer resourceId)
      throws TikaException, IOException, SAXException {
    ContentHandler handler = new DefaultHandler();
    Metadata metadata = new Metadata();
    Parser parser = new Mp3Parser();
    ParseContext parseCtx = new ParseContext();
    parser.parse(inputStream, handler, metadata, parseCtx);
    inputStream.close();

    Optional<String> length = Optional.of(metadata.get("xmpDM:duration"))
        .map(Double::valueOf)
        .map(Double::longValue)
        .map(Duration::ofMillis)
        .map(d -> String.format("%d:%d", d.toMinutesPart(), d.toSecondsPart()));
    log.info("Metadata for resource {} was parsed", resourceId);
    return Mp3DetailsDTO.builder()
        .resourceId(resourceId)
        .title(metadata.get("title"))
        .artist(metadata.get("xmpDM:artist"))
        .album(metadata.get("xmpDM:album"))
        .length(length.orElse(null))
        .releaseDate(metadata.get("xmpDM:releaseDate"))
        .build();
  }
}
