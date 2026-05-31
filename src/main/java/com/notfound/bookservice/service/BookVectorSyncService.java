package com.notfound.bookservice.service;

import java.util.UUID;

public interface BookVectorSyncService {
    void index(UUID bookId);

    void remove(UUID bookId);
}
