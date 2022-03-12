package com.example.indeedjobfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableConfigurationProperties(ConfigLoader.class)
public class IndeedJobFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndeedJobFinderApplication.class, args);
    }

    @Bean
    public WebClient getWebClientBuilder() {
        return WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(128 * 1024 * 1024))
                        .build())
                .build();
    }

}
