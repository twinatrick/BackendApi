package com.example.backedapi.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocConfig {

    @Bean
    public GroupedOpenApi myApi() {
        return GroupedOpenApi.builder()
                .group("BackendApi") // 自定義 API 分組名稱
                .pathsToMatch("/backend/**") // 只包含 /backend/** 路徑下的 API
                .build();
    }
}
