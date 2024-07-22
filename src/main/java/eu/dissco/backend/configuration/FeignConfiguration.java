package eu.dissco.backend.configuration;

import feign.Client;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignConfiguration {

  @Bean
  public Client feignClient() {
    return new Client.Default(getSSLSocketFactory(), new NoopHostnameVerifier());
  }


  private SSLSocketFactory getSSLSocketFactory() {
    try {
      TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          return true;
        }
      };
      SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
          .build();
      return sslContext.getSocketFactory();
    } catch (Exception e) {
      log.error("An exception occurred when pushing annotationRequests to annotationRequests-processor", e);
    }
    return null;
  }
}
