package com.example.BackendApi.Service.impl;

import com.example.BackendApi.Service.IGeminiService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of IGeminiService using Google Gemini REST API directly.
 */
@Slf4j
@Service
public class GeminiService implements IGeminiService {

    private final RestTemplate restTemplate;
    private final Gson gson;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.gson = new Gson();
    }

    @Override
    public List<Map<String, String>> analyzeJobPostings(String companyName, String htmlContent) {
        try {
            String truncatedHtml = htmlContent.length() > 50000
                    ? htmlContent.substring(0, 50000)
                    : htmlContent;

            String userPrompt = String.format("""
                    公司名稱: %s
                    
                    HTML 內容:
                    %s
                    """, companyName, truncatedHtml);

            String requestBody = buildRequestBody(userPrompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            String url = apiUrl + "?key=" + apiKey;
            log.info("Calling Gemini API for company: {}", companyName);
            log.debug("Gemini API URL: {}", url);

            String response = restTemplate.postForObject(url, request, String.class);

            if (response == null) {
                log.warn("Gemini API returned null response for company: {}", companyName);
                return List.of();
            }

            return parseResponse(response);
        } catch (Exception e) {
            log.error("Failed to call Gemini API for company: {}", companyName, e);
            return List.of();
        }
    }

    private String buildRequestBody(String prompt) {
        JsonObject requestBody = new JsonObject();

        // System instruction
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", "你是一個專業的職缺分析助手，請以 JSON 陣列格式回覆。");
        systemParts.add(systemPart);
        systemInstruction.add("parts", systemParts);
        requestBody.add("system_instruction", systemInstruction);

        // User content
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        content.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Generation config
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.1);
        generationConfig.addProperty("maxOutputTokens", 8192);
        requestBody.add("generationConfig", generationConfig);

        return gson.toJson(requestBody);
    }

    private List<Map<String, String>> parseResponse(String response) {
        try {
            JsonObject responseObj = gson.fromJson(response, JsonObject.class);
            JsonArray candidates = responseObj.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) {
                log.warn("No candidates found in Gemini response");
                return List.of();
            }

            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            JsonObject content = firstCandidate.getAsJsonObject("content");
            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null || parts.size() == 0) {
                log.warn("No parts found in Gemini response candidate");
                return List.of();
            }

            String text = parts.get(0).getAsJsonObject().get("text").getAsString();

            // Extract JSON array from text
            int start = text.indexOf('[');
            int end = text.lastIndexOf(']');
            if (start == -1 || end == -1 || end <= start) {
                log.warn("No JSON array found in Gemini response text: {}", text);
                return List.of();
            }

            String jsonArray = text.substring(start, end + 1);
            JsonArray jobs = gson.fromJson(jsonArray, JsonArray.class);

            List<Map<String, String>> result = new ArrayList<>();
            for (JsonElement jobElement : jobs) {
                JsonObject jobObj = jobElement.getAsJsonObject();
                Map<String, String> jobMap = new LinkedHashMap<>();
                for (String key : new String[]{"title", "url", "description", "requirements", "responsibilities", "salaryRange"}) {
                    JsonElement value = jobObj.get(key);
                    jobMap.put(key, value != null && !value.isJsonNull() ? value.getAsString() : "");
                }
                result.add(jobMap);
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
            return List.of();
        }
    }
}
