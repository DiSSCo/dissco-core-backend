package eu.dissco.backend.configuration;

import static eu.dissco.backend.controller.BaseController.DATE_STRING;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InstantSerializer extends JsonSerializer<Instant> {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_STRING).withZone(
      ZoneOffset.UTC);


  /*
  1 - 2022-11-01T09:59:24Z gd
  2 - 2022-11-01T09:59:24Z
  3 - 2022-11-01T09:59:24Z
  4 - 2022-11-01T09:59:24Z
  5 - 2022-11-01T09:59:24Z
  6 - 2022-11-01T09:59:24Z
  7 - 2022-11-01T09:59:24Z
  8 - 2022-11-01T09:59:24Z
  9 - 2022-11-01T09:59:24Z
      2022-11-01T09:59:24Z

   */
  @Override
  public void serialize(Instant value, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) {
    try {
      jsonGenerator.writeString(formatter.format(value));
    } catch (IOException e) {
      log.error("An error has occurred serializing a date. More information: {}", e.getMessage());
    }
  }
}
