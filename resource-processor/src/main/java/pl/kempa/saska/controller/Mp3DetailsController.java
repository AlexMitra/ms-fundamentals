package pl.kempa.saska.controller;

import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import pl.kempa.saska.dto.Mp3DetailsDTO;
import pl.kempa.saska.processor.Mp3DetailsProcessor;

@RestController
@RequestMapping(value = "/api/mp3-details")
public class Mp3DetailsController {
  @Autowired
  private Mp3DetailsProcessor processor;

  @PostMapping
  public ResponseEntity<Mp3DetailsDTO> getDetails(@RequestPart MultipartFile file)
      throws TikaException, IOException, SAXException {
    return ResponseEntity.ok(processor.processMp3(file));
  }
}
