package com.JieAI.AIquestion.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AIConfig {
    /**
     * 智谱AI key
     */
    public String aiKey;

    @Bean
    public ClientV4 getClientV4() {
        return new ClientV4.Builder(aiKey).build();
    }
}
