package com.nexus.rinde.dashboard.infrastructure.remote;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RemoteFleetConfiguration {

  @Bean
  public RestClient dashboardFleetRestClient(
      RestClient.Builder builder,
      @Value("${rinde.fleet.base-url:http://localhost:8083}") String baseUrl) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3_000);
    factory.setReadTimeout(5_000);
    return builder.baseUrl(baseUrl).requestFactory(factory).build();
  }
}
