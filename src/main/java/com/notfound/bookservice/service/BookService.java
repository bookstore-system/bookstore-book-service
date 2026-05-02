package com.notfound.bookservice.service;

import com.notfound.bookservice.model.dto.request.CreateBookRequest;
import com.notfound.bookservice.model.dto.request.UpdateBookRequest;
import com.notfound.bookservice.model.dto.response.AuthorResponse;
import com.notfound.bookservice.model.dto.response.BookResponse;
import com.notfound.bookservice.model.dto.response.BookSummaryResponse;
import com.notfound.bookservice.model.dto.response.CategoryResponse;
import com.notfound.bookservice.model.dto.response.PageResponse;

import java.util.List;
import java.util.UUID;

public interface BookService {
    PageResponse<BookSummaryResponse> getBooks(Integer page, Integer size, String keyword);
    BookResponse getBookById(UUID id);
    BookResponse createBook(CreateBookRequest request);
    BookResponse updateBook(UUID id, UpdateBookRequest request);
    void deleteBook(UUID id);
    boolean verifyBook(UUID id);
    List<CategoryResponse> getCategories();
    List<AuthorResponse> getAuthors();
}
