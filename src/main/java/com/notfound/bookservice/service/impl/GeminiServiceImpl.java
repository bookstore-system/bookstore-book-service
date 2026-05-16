package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.service.GeminiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public double[] embed(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured (GEMINI_API_KEY)");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key="
                + apiKey;

        Map<String, Object> body = Map.of(
                "content", Map.of(
                        "parts", List.of(Map.of("text", text))));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            if (response == null) {
                throw new RuntimeException("Gemini API returned null response");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> embeddingMap = (Map<String, Object>) response.get("embedding");
            if (embeddingMap == null) {
                throw new RuntimeException("No embedding found in Gemini response");
            }

            @SuppressWarnings("unchecked")
            List<Double> values = (List<Double>) embeddingMap.get("values");
            if (values == null || values.isEmpty()) {
                throw new RuntimeException("Empty embedding values from Gemini");
            }

            return values.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (Exception e) {
            log.error("Failed to generate embedding from Gemini: {}", e.getMessage());
            throw new RuntimeException("Failed to generate embedding from Gemini", e);
        }
    }
}
