package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.Category;
import com.notfound.bookservice.service.BookVectorSyncService;
import com.notfound.bookservice.service.GeminiService;
import com.notfound.bookservice.service.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookVectorSyncServiceImpl implements BookVectorSyncService {

    private final GeminiService geminiService;
    private final QdrantService qdrantService;

    @Override
    public void index(Book book) {
        String embeddingText = buildEmbeddingText(book);
        log.info("Syncing book vector to Qdrant: {}", book.getId());

        try {
            double[] vector = geminiService.embed(embeddingText);
            qdrantService.insertBookVector(book.getId(), vector, book);
            log.info("Indexed book in Qdrant: {}", book.getId());
        } catch (Exception e) {
            log.error("Failed to index book {} in Qdrant: {}", book.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void remove(UUID bookId) {
        try {
            qdrantService.deleteBookVector(bookId);
        } catch (Exception e) {
            log.error("Failed to delete book {} from Qdrant: {}", bookId, e.getMessage(), e);
        }
    }

    private String buildEmbeddingText(Book book) {
        StringBuilder text = new StringBuilder(book.getTitle());
        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            String categoryNames = book.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));
            text.append(". ").append(categoryNames);
        }
        if (book.getDescription() != null && !book.getDescription().isBlank()) {
            text.append(". ").append(book.getDescription());
        }
        return text.toString();
    }
}
