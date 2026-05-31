package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.Category;
import com.notfound.bookservice.repository.BookRepository;
import com.notfound.bookservice.service.BookVectorSyncService;
import com.notfound.bookservice.service.GeminiService;
import com.notfound.bookservice.service.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookVectorSyncServiceImpl implements BookVectorSyncService {

    private final BookRepository bookRepository;
    private final GeminiService geminiService;
    private final QdrantService qdrantService;

    @Override
    @Async("bookVectorTaskExecutor")
    @Transactional(readOnly = true)
    public void index(UUID bookId) {
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
            String embeddingText = buildEmbeddingText(book);
            log.info("Syncing book vector to Qdrant: {}", bookId);
            double[] vector = geminiService.embed(embeddingText);
            qdrantService.insertBookVector(bookId, vector, book);
            log.info("Indexed book in Qdrant: {}", bookId);
        } catch (Exception e) {
            log.error("Failed to index book {} in Qdrant: {}", bookId, e.getMessage(), e);
        }
    }

    @Override
    @Async("bookVectorTaskExecutor")
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
