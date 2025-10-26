package com.DocAITutor.DocAITutor.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;



@Configuration
public class OpenRouterConfig {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Bean
    public WebClient openRouterWebClient() {
        return WebClient.builder()
                .baseUrl("https://openrouter.ai/api/v1/chat/completions") 
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }
}

