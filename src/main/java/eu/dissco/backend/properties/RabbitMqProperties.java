package eu.dissco.backend.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMqProperties {

  @NotBlank
  private String masExchangeName = "mas-exchange";
  @NotBlank
  private String provenanceExchange = "provenance-exchange";
  @NotBlank
  private String provenanceRoutingPrefix = "provenance";
  @NotBlank
  private String virtualCollectionExchange = "virtual-collection-exchange";
  @NotBlank
  private String virtualCollectionRoutingKey = "virtual-collection";
}
