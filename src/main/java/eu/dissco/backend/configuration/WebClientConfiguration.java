package eu.dissco.backend.configuration;

import eu.dissco.backend.client.HandleClient;
import eu.dissco.backend.component.FdoRecordComponent;
import eu.dissco.backend.properties.WebConnectionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

  private final WebConnectionProperties properties;
  private final JsonMapper mapper;

  @Bean
  public OAuth2AuthorizedClientManager authorizedClientManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientService clientService) {
    var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder
        .builder()
        .refreshToken()
        .clientCredentials()
        .build();
    var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
        clientRegistrationRepository, clientService
    );
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
    return authorizedClientManager;
  }

  @Bean
  public HandleClient handleClient(OAuth2AuthorizedClientManager authorizedClientManager) {
    // Error Exchange filtering
    ExchangeFilterFunction errorResponseFilter = ExchangeFilterFunction
        .ofResponseProcessor(WebClientErrorHandling::exchangeFilterResponseProcessor);
    // Set up Oauth2
    var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
        authorizedClientManager);
    oauth2Client.setDefaultClientRegistrationId("dissco");
    // Build web client
    var webClient = WebClient.builder()
        .apply(oauth2Client.oauth2Configuration())
        .filter(errorResponseFilter)
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
        .baseUrl(properties.getHandleEndpoint())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
    // Create factory for client proxies
    var proxyFactory = HttpServiceProxyFactory.builder()
        .exchangeAdapter(WebClientAdapter.create(webClient))
        .build();
    // Create client proxy
    return proxyFactory.createClient(HandleClient.class);
  }

  @Bean
  public WebClient handleClient1(OAuth2AuthorizedClientManager authorizedClientManager) {
    var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
        authorizedClientManager);
    oauth2Client.setDefaultClientRegistrationId("dissco");
    return WebClient.builder()
        .apply(oauth2Client.oauth2Configuration())
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
        .baseUrl(properties.getHandleEndpoint())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Bean(name = "fdoRecordBuilder")
  public FdoRecordComponent fdoRecordBuilder() {
    return new FdoRecordComponent(mapper);
  }

}
