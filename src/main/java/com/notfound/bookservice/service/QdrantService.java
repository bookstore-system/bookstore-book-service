package com.notfound.bookservice.service;

import com.notfound.bookservice.model.entity.Book;

import java.util.List;
import java.util.UUID;

public interface QdrantService {
    void insertBookVector(UUID bookId, double[] vector, Book book);

    List<String> searchBookIds(double[] queryVector, int limit);
}
