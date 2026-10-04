package com.giovanni.similarproducts.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ExternalApiClientConfig {

        @Bean
        public RestClient productsRestClient(
                        @Value("${products.api.base-url}") String baseUrl,
                        @Value("${products.api.timeout}") Duration timeout) {
                JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                                HttpClient.newBuilder().connectTimeout(timeout).build());
                requestFactory.setReadTimeout(timeout);
                return RestClient.builder()
                                .baseUrl(baseUrl)
                                .requestFactory(requestFactory)
                                .build();
        }
}
