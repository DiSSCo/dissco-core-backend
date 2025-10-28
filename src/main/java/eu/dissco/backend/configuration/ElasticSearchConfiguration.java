package eu.dissco.backend.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;
import eu.dissco.backend.properties.ElasticSearchProperties;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ElasticSearchConfiguration {

  private final ElasticSearchProperties properties;

  @Bean
  public ElasticsearchClient elasticsearchClient() {
    var creds = Base64.getEncoder()
        .encodeToString((properties.getUsername() + ":" + properties.getPassword()).getBytes());
    Rest5ClientBuilder restClient = Rest5Client
        .builder(new HttpHost(properties.getHostname(), properties.getPort()))
        .setDefaultHeaders(new Header[]{
            new BasicHeader("Authorization", "Basic " + creds)
        });
    ElasticsearchTransport transport = new Rest5ClientTransport(restClient.build(),
        new JacksonJsonpMapper());
    return new ElasticsearchClient(transport);
  }

}
