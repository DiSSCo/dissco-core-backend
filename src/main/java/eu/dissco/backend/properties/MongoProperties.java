package eu.dissco.backend.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "mongo")
public class MongoProperties {

  @NotBlank
  private String connectionString;

  @NotBlank
  private String database;

}
