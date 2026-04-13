package eu.dissco.backend.configuration;

import static lombok.Lombok.sneakyThrow;

import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.client.HandleClient;
import eu.dissco.backend.client.MasClient;
import eu.dissco.backend.client.ProcessorClient;
import eu.dissco.backend.exceptions.WebAuthenticationException;
import eu.dissco.backend.exceptions.WebProcessingFailedException;
import eu.dissco.backend.properties.WebConnectionProperties;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RestClientConfiguration {

	private final WebConnectionProperties properties;

	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(
			ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService clientService) {
		var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
			.refreshToken()
			.clientCredentials()
			.build();
		var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
				clientRegistrationRepository, clientService);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
		return authorizedClientManager;
	}

	@Bean
	public HandleClient handleClient(OAuth2AuthorizedClientManager authorizedClientManager) {
		// Error Exchange filtering
		var proxyFactory = createProxyFactory("Handle", properties.getHandleEndpoint(), authorizedClientManager);
		// Create client proxy
		return proxyFactory.createClient(HandleClient.class);
	}

	@Bean
	public AnnotationClient annotationClient() {
		var proxyFactory = createProxyFactory("Annotation", properties.getAnnotationEndpoint(), null);
		// Create client proxy
		return proxyFactory.createClient(AnnotationClient.class);
	}

	@Bean
	public MasClient masClient() {
		var proxyFactory = createProxyFactory("MAS Scheduler", properties.getMasEndpoint(), null);
		// Create client proxy
		return proxyFactory.createClient(MasClient.class);
	}

	@Bean
	public ProcessorClient processorClient(OAuth2AuthorizedClientManager authorizedClientManager) {
		var proxyFactory = createProxyFactory("Processing", properties.getProcessorEndpoint(), authorizedClientManager);
		// Create client proxy
		return proxyFactory.createClient(ProcessorClient.class);
	}

	private HttpServiceProxyFactory createProxyFactory(String serviceName, String endpoint,
			OAuth2AuthorizedClientManager authorizedClientManager) {
		// Create RestClient
		var restClientBuilder = RestClient.builder()
			// On status error, log the response and throw a WebProcessingFailedException
			.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
				var body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
				if (HttpStatus.UNAUTHORIZED.equals(response.getStatusCode())) {
					log.error("Unable to authenticate with the {} Service: {}", serviceName, body);
					throw sneakyThrow(
							new WebAuthenticationException("Unable to authenticate with " + serviceName + " service"));
				}
				log.error("Unable to communicate with the {} service. Status: {}, Body: {}", serviceName,
						response.getStatusCode(), body);
				throw sneakyThrow(new WebProcessingFailedException(
						"An error has occurred communicating with an external service"));
			})
			.baseUrl(endpoint)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		if (authorizedClientManager != null) {
			var interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
			interceptor.setClientRegistrationIdResolver(_ -> "dissco");
			restClientBuilder.requestInterceptor(interceptor);
		}
		// Create factory for client proxies
		return HttpServiceProxyFactory.builder()
			.exchangeAdapter(RestClientAdapter.create(restClientBuilder.build()))
			.build();
	}

}
