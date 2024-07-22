package eu.dissco.backend.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.backend.component.FdoRecordComponent;
import eu.dissco.backend.properties.FdoProperties;
import eu.dissco.backend.properties.WebConnectionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

  private final WebConnectionProperties properties;
  private final ObjectMapper mapper;
  private final FdoProperties fdoProperties;

  @Bean(name = "tokenClient")
  public WebClient tokenClient() {
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
        .baseUrl(properties.getTokenEndpoint())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .build();
  }

  @Bean(name = "handleClient")
  public WebClient handleClient() {
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
        .baseUrl(properties.getHandleEndpoint())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Bean(name = "fdoRecordBuilder")
  public FdoRecordComponent fdoRecordBuilder() {
    return new FdoRecordComponent(mapper, fdoProperties);
  }

}
