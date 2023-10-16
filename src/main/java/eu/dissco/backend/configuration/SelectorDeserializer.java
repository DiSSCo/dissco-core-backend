package eu.dissco.backend.configuration;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.domain.annotation.ClassValueSelector;
import eu.dissco.backend.domain.annotation.FieldValueSelector;
import eu.dissco.backend.domain.annotation.FragmentSelector;
import eu.dissco.backend.domain.annotation.Selector;
import java.io.IOException;

public class SelectorDeserializer extends JsonDeserializer<Selector> {

  @Override
  public Selector deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
    switch (node.get("ods:type").asText()) {
      case ("FieldValueSelector") -> {
        return mapper.treeToValue(node, FieldValueSelector.class);
      }
      case ("ClassValueSelector") -> {
        return mapper.treeToValue(node, ClassValueSelector.class);
      }
      case ("FragmentSelector") -> {
        return mapper.treeToValue(node, FragmentSelector.class);
      }
      default -> throw new IOException("Unable to parse selector type");
    }
  }
}
