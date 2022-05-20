package eu.dissco.backend.configuration;

import eu.dissco.backend.properties.CordraProperties;
import lombok.RequiredArgsConstructor;
import net.cnri.cordra.api.CordraClient;
import net.cnri.cordra.api.CordraException;
import net.cnri.cordra.api.TokenUsingHttpCordraClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CordraRestConfiguration {

  private final CordraProperties properties;

  @Bean
  CordraClient cordraClient() throws CordraException {
    return new TokenUsingHttpCordraClient(properties.getHost(), properties.getUsername(),
        properties.getPassword());
  }

}
