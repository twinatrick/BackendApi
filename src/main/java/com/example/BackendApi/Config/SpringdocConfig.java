package com.example.BackendApi.Config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocConfig {

    @Bean
    public GroupedOpenApi myApi() {
        return GroupedOpenApi.builder()
                .group("BackendApi") // 自定義 API 分組名稱
                .pathsToMatch("/**") // 包含所有 API 路徑
                .build();
    }
}
