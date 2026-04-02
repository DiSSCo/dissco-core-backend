package eu.dissco.backend.configuration;

import eu.dissco.backend.client.AnnotationClient;
import eu.dissco.backend.client.HandleClient;
import eu.dissco.backend.client.MasClient;
import eu.dissco.backend.client.ProcessorClient;
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

@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

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
		var proxyFactory = createProxyFactory("Handle", properties.getAnnotationEndpoint(), authorizedClientManager);
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
		var errorResponseFilter = ExchangeFilterFunction
			.ofResponseProcessor(r -> WebClientErrorHandling.exchangeFilterResponseProcessor(r, serviceName));
		var webClientBuilder = WebClient.builder()
			.filter(errorResponseFilter)
			.clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
			.baseUrl(endpoint)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		if (authorizedClientManager != null) {
			webClientBuilder.apply(createOauth2Client(authorizedClientManager).oauth2Configuration());
		}
		// Create factory for client proxies
		return HttpServiceProxyFactory.builder()
			.exchangeAdapter(WebClientAdapter.create(webClientBuilder.build()))
			.build();
	}

	private static ServletOAuth2AuthorizedClientExchangeFilterFunction createOauth2Client(
			OAuth2AuthorizedClientManager authorizedClientManager) {
		// Set up Oauth2
		var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
		oauth2Client.setDefaultClientRegistrationId("dissco");
		return oauth2Client;
	}

}
