package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.client.GeminiEmbeddingClient;
import com.notfound.bookservice.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

    private final GeminiEmbeddingClient geminiEmbeddingClient;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.embedding.model:gemini-embedding-001}")
    private String embeddingModel;

    @Value("${gemini.embedding.output-dimensionality:768}")
    private int outputDimensionality;

    @Override
    public double[] embed(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured (GEMINI_API_KEY)");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("content", Map.of("parts", List.of(Map.of("text", text))));
        if (outputDimensionality > 0) {
            body.put("outputDimensionality", outputDimensionality);
        }

        try {
            Map<String, Object> response = geminiEmbeddingClient.embedContent(embeddingModel, apiKey, body);

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
