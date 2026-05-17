package com.notfound.bookservice.client.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class QdrantFeignConfig {

    @Bean
    public RequestInterceptor qdrantApiKeyInterceptor(
            @Value("${qdrant.api.key:}") String apiKey) {
        return template -> {
            if (apiKey != null && !apiKey.isBlank()) {
                template.header("api-key", apiKey);
            }
        };
    }
}
