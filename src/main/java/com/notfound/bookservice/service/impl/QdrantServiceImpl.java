package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.client.QdrantClient;
import com.notfound.bookservice.model.entity.Author;
import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.Category;
import com.notfound.bookservice.service.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QdrantServiceImpl implements QdrantService {

    private final QdrantClient qdrantClient;

    @Value("${clients.qdrant.url:}")
    private String qdrantUrl;

    @Value("${qdrant.api.key:}")
    private String apiKey;

    @Override
    public void insertBookVector(UUID bookId, double[] vector, Book book) {
        assertQdrantConfigured();

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

        try {
            qdrantClient.upsertPoints(true, body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert vector to Qdrant", e);
        }
    }

    @Override
    public List<String> searchBookIds(double[] queryVector, int limit) {
        if (!isQdrantConfigured()) {
            log.warn("Qdrant is not configured, skipping vector search");
            return List.of();
        }

        Map<String, Object> body = new HashMap<>();
        body.put("vector", queryVector);
        body.put("limit", limit);

        try {
            Map<String, Object> response = qdrantClient.searchPoints(body);

            if (response == null || !response.containsKey("result")) {
                log.warn("Empty search result from Qdrant");
                return List.of();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) response.get("result");

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
        if (!isQdrantConfigured()) {
            log.warn("Qdrant is not configured, skipping vector delete for book {}", bookId);
            return;
        }

        Map<String, Object> body = Map.of("points", List.of(bookId.toString()));

        try {
            qdrantClient.deletePoints(true, body);
            log.info("Deleted vector from Qdrant for book: {}", bookId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete vector from Qdrant", e);
        }
    }

    private void assertQdrantConfigured() {
        if (!isQdrantConfigured()) {
            throw new IllegalStateException("Qdrant is not configured (QDRANT_URL, QDRANT_API_KEY)");
        }
    }

    private boolean isQdrantConfigured() {
        return qdrantUrl != null && !qdrantUrl.isBlank() && apiKey != null && !apiKey.isBlank();
    }
}
