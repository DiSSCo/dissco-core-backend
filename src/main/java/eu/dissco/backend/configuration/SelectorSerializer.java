package eu.dissco.backend.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import eu.dissco.backend.domain.annotation.Selector;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectorSerializer extends JsonSerializer<Selector> {

  @Override
  public void serialize(Selector selector, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) throws IOException {
      try {
        jsonGenerator.writeString(selector.toString());
      } catch (IOException e) {
        log.error("An error has occurred serializing a date. More information: {}", e.getMessage());
      }

  }
}
