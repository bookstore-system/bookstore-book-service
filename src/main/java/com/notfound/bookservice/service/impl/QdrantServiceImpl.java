package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.model.entity.Author;
import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.Category;
import com.notfound.bookservice.service.QdrantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QdrantServiceImpl implements QdrantService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${qdrant.url:}")
    private String qdrantUrl;

    @Value("${qdrant.api.key:}")
    private String apiKey;

    @Override
    public void insertBookVector(UUID bookId, double[] vector, Book book) {
        if (qdrantUrl == null || qdrantUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Qdrant is not configured (QDRANT_URL, QDRANT_API_KEY)");
        }

        String url = qdrantUrl + "/collections/books/points?wait=true";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        List<String> authorNames = book.getAuthors() == null
                ? List.of()
                : book.getAuthors().stream().map(Author::getName).collect(Collectors.toList());

        List<String> categoryIds = book.getCategories() == null
                ? List.of()
                : book.getCategories().stream().map(c -> c.getId().toString()).collect(Collectors.toList());

        List<String> categoryNames = book.getCategories() == null
                ? List.of()
                : book.getCategories().stream().map(Category::getName).collect(Collectors.toList());

        Map<String, Object> payload = new HashMap<>();
        payload.put("book_id", bookId.toString());
        payload.put("title", book.getTitle());
        payload.put("authors", authorNames);
        payload.put("category_ids", categoryIds);
        payload.put("categories", categoryNames);
        payload.put("description", book.getDescription());
        payload.put("isbn", book.getIsbn());
        payload.put("price", book.getPrice());

        List<Double> vectorList = new ArrayList<>();
        for (double v : vector) {
            vectorList.add(v);
        }

        Map<String, Object> namedVector = new HashMap<>();
        namedVector.put("", vectorList);

        Map<String, Object> point = new HashMap<>();
        point.put("id", bookId.toString());
        point.put("vector", namedVector);
        point.put("payload", payload);

        Map<String, Object> body = new HashMap<>();
        body.put("points", List.of(point));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert vector to Qdrant", e);
        }
    }

    @Override
    public List<String> searchBookIds(double[] queryVector, int limit) {
        if (qdrantUrl == null || qdrantUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.warn("Qdrant is not configured, skipping vector search");
            return List.of();
        }

        String url = qdrantUrl + "/collections/books/points/search";

        Map<String, Object> body = new HashMap<>();
        body.put("vector", queryVector);
        body.put("limit", limit);

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getBody() == null || !response.getBody().containsKey("result")) {
                log.warn("Empty search result from Qdrant");
                return List.of();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) response.getBody().get("result");

            return result.stream()
                    .map(item -> item.get("id").toString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Qdrant search failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public void deleteBookVector(UUID bookId) {
        if (qdrantUrl == null || qdrantUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.warn("Qdrant is not configured, skipping vector delete for book {}", bookId);
            return;
        }

        String url = qdrantUrl + "/collections/books/points/delete?wait=true";

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("points", List.of(bookId.toString()));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            log.info("Deleted vector from Qdrant for book: {}", bookId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete vector from Qdrant", e);
        }
    }
}
