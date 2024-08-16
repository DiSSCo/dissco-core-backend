package eu.dissco.backend.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@Data
@RequiredArgsConstructor
@ConfigurationProperties("fdo")
public class FdoProperties {

  @NotBlank
  private final String agent;

}
