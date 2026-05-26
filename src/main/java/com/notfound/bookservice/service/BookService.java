package com.notfound.bookservice.service;

import com.notfound.bookservice.model.dto.request.BookFilterRequest;
import com.notfound.bookservice.model.dto.request.BookOptionsRequest;
import com.notfound.bookservice.model.dto.request.BookSortRequest;
import com.notfound.bookservice.model.dto.request.CreateBookRequest;
import com.notfound.bookservice.model.dto.request.CreateCategoryRequest;
import com.notfound.bookservice.model.dto.request.ReduceStockRequest;
import com.notfound.bookservice.model.dto.request.UpdateBookRequest;
import com.notfound.bookservice.model.dto.response.BatchBookDetailItemResponse;
import com.notfound.bookservice.model.dto.response.BatchBooksResponse;
import com.notfound.bookservice.model.dto.response.BookResponse;
import com.notfound.bookservice.model.dto.response.BookExistsResponse;
import com.notfound.bookservice.model.dto.response.BookSummaryResponse;
import com.notfound.bookservice.model.dto.response.CategoryBooksResponse;
import com.notfound.bookservice.model.dto.response.CategoryResponse;
import com.notfound.bookservice.model.dto.response.CategoryWithBookResponse;
import com.notfound.bookservice.model.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import com.notfound.bookservice.model.dto.response.ValidateBookIdsResponse;

import java.util.List;
import java.util.UUID;

public interface BookService {
    PageResponse<BookSummaryResponse> getBooks(Integer page, Integer size);

    PageResponse<BookSummaryResponse> searchBooks(String keyword, Integer page, Integer size);
    PageResponse<BookSummaryResponse> getAllBooksOption(BookOptionsRequest request);
    PageResponse<BookSummaryResponse> findByFilters(BookFilterRequest request);
    PageResponse<BookSummaryResponse> getSortedBooks(BookSortRequest request);
    List<BookSummaryResponse> getBestSellingBooks(Integer limit);
    List<BookSummaryResponse> getSuggestedBooks(Integer limit);
    List<CategoryBooksResponse> getBooksByPopularCategories(Integer categoryLimit, Integer bookLimit);
    PageResponse<BookSummaryResponse> getBooksByCategory(UUID categoryId, Integer page, Integer pageSize);
    BookResponse getBookById(UUID id);
    BookResponse createBook(CreateBookRequest request);
    BookResponse updateBook(UUID id, UpdateBookRequest request);
    void deleteBook(UUID id);
    boolean verifyBook(UUID id);
    BatchBooksResponse getBooksBatch(List<UUID> ids);
    BookExistsResponse checkBookExists(UUID id);
    ValidateBookIdsResponse validateBookIds(List<UUID> bookIds);
    List<BatchBookDetailItemResponse> getBatchBookDetails(List<UUID> bookIds);
    void reduceStock(ReduceStockRequest request);
    List<CategoryResponse> getCategories();

    CategoryResponse getCategoryById(UUID categoryId);

    Page<CategoryResponse> getCategoriesPaged(int page, int size);

    List<CategoryResponse> getPopularCategories(Integer limit);

    List<CategoryWithBookResponse> getCategoriesWithSampleBook();

    CategoryResponse createCategory(CreateCategoryRequest request);

}
