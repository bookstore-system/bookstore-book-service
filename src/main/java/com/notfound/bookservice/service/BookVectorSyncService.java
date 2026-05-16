package com.notfound.bookservice.service;

import com.notfound.bookservice.model.entity.Book;

import java.util.UUID;

public interface BookVectorSyncService {
    void index(Book book);

    void remove(UUID bookId);
}
