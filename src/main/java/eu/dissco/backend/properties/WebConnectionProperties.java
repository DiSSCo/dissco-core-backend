package eu.dissco.backend.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("endpoint")
public class WebConnectionProperties {

  @NotBlank
  private String handleEndpoint;

}
