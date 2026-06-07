package com.example.BackendApi.Service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GitHubModelsService extends BaseOpenAiService {

    @Value("${github.models.api.key}")
    private String apiKey;

    @Value("${github.models.api.url}")
    private String apiUrl;

    @Value("${github.models.api.model}")
    private String model;

    @Override
    protected String getApiKey() {
        return apiKey;
    }

    @Override
    protected String getApiUrl() {
        return apiUrl;
    }

    @Override
    protected String getModelName() {
        return model;
    }

    @Override
    public List<Map<String, String>> analyzeJobPostings(String companyName, String htmlContent) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GitHub Models API key not configured, skipping");
            return List.of();
        }
        return super.analyzeJobPostings(companyName, htmlContent);
    }
}
