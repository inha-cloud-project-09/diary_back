package com.diary.api.domain.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix = "openai")
@Configuration
public class OpenAIConfig {
    private String apiKey;
    private String model;
    private Integer maxTokens;
    private Double temperature;
    private Timeout timeout = new Timeout();

    @Getter
    @Setter
    public static class Timeout {
        private Integer connect;
        private Integer read;
        private Integer write;
    }
}