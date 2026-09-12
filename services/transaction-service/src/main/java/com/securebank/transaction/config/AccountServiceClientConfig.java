package com.securebank.transaction.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    AccountServiceClientProperties.class
)
public class AccountServiceClientConfig {

    @Bean
    @Qualifier("accountServiceRestClient")
    public RestClient accountServiceRestClient(
        RestClient.Builder builder,
        AccountServiceClientProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory =
            new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(2_000);
        requestFactory.setReadTimeout(5_000);

        return builder
            .baseUrl(properties.baseUrl().toString())
            .defaultHeader(
                "X-Internal-Api-Key",
                properties.apiKey()
            )
            .requestFactory(requestFactory)
            .build();
    }
}
