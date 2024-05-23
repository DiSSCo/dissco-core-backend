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
  private final String mjrType = "https://hdl.handle.net/21.T11148/532ce6796e2828dd2be6";

  @NotBlank
  private final String agent;

}
