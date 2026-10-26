package com.nexus.rinde.iam.infrastructure.remote;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** La consulta de identidad tiene tiempos de espera acotados y una dirección configurable. */
@Configuration
@ConditionalOnProperty(name = "rinde.iam.remote-enabled", havingValue = "true")
public class RemoteIamConfiguration {
  @Bean
  public RestClient iamRestClient(RestClient.Builder builder,
      @Value("${rinde.iam.base-url:http://localhost:8081}") String baseUrl) {
    var requests = new SimpleClientHttpRequestFactory();
    requests.setConnectTimeout(3_000);
    requests.setReadTimeout(5_000);
    return builder.baseUrl(baseUrl).requestFactory(requests).build();
  }
}
