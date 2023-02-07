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
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pl.kempa.saska.dto.Mp3DetailsDTO;

@Component
public class Mp3DetailsProcessor {
  public Mp3DetailsDTO processMp3(MultipartFile file)
      throws TikaException, IOException, SAXException {
    InputStream inputStream = file.getInputStream();
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
    return Mp3DetailsDTO.builder()
        .fileName(file.getOriginalFilename())
        .fileSize(file.getSize())
        .title(metadata.get("title"))
        .artist(metadata.get("xmpDM:artist"))
        .album(metadata.get("xmpDM:album"))
        .length(length.orElse(null))
        .releaseDate(metadata.get("xmpDM:releaseDate"))
        .build();
  }
}
