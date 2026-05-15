package com.notfound.bookservice.controller;

import com.notfound.bookservice.model.dto.request.CreateBookRequest;
import com.notfound.bookservice.model.dto.request.BatchBookDetailsRequest;
import com.notfound.bookservice.model.dto.request.BatchBooksRequest;
import com.notfound.bookservice.model.dto.request.BookFilterRequest;
import com.notfound.bookservice.model.dto.request.BookOptionsRequest;
import com.notfound.bookservice.model.dto.request.BookSortRequest;
import com.notfound.bookservice.model.dto.request.ReduceStockRequest;
import com.notfound.bookservice.model.dto.request.UpdateBookRequest;
import com.notfound.bookservice.model.dto.request.ValidateBookIdsRequest;
import com.notfound.bookservice.model.dto.response.ApiResponse;
import com.notfound.bookservice.model.dto.response.BatchBookDetailItemResponse;
import com.notfound.bookservice.model.dto.response.BatchBooksResponse;
import com.notfound.bookservice.model.dto.response.BookResponse;
import com.notfound.bookservice.model.dto.response.BookExistsResponse;
import com.notfound.bookservice.model.dto.response.BookSummaryResponse;
import com.notfound.bookservice.model.dto.response.CategoryBooksResponse;
import com.notfound.bookservice.model.dto.response.PageResponse;
import com.notfound.bookservice.model.dto.response.ValidateBookIdsResponse;
import com.notfound.bookservice.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "CRUD sách, lọc, batch và tồn kho")
public class BookController {
    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Lấy danh sách sách có phân trang")
    public ResponseEntity<ApiResponse<PageResponse<BookSummaryResponse>>> getBooks(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBooks(page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm sách theo từ khóa")
    public ResponseEntity<ApiResponse<PageResponse<BookSummaryResponse>>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(ApiResponse.success(bookService.searchBooks(keyword, page, size)));
    }

    @GetMapping("/options")
    @Operation(summary = "Lấy sách theo tùy chọn lọc và sắp xếp")
    public ResponseEntity<ApiResponse<PageResponse<BookSummaryResponse>>> getBookOptions(
            @Valid @ModelAttribute BookOptionsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getAllBooksOption(request)));
    }

    @GetMapping("/filter")
    @Operation(summary = "Lọc sách theo tiêu chí")
    public ResponseEntity<ApiResponse<PageResponse<BookSummaryResponse>>> filterBooks(
            @Valid @ModelAttribute BookFilterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookService.findByFilters(request)));
    }

    @GetMapping("/sorted")
    @Operation(summary = "Sắp xếp sách")
    public ResponseEntity<ApiResponse<PageResponse<BookSummaryResponse>>> getSortedBooks(
            @Valid @ModelAttribute BookSortRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getSortedBooks(request)));
    }

    @GetMapping("/best-selling")
    @Operation(summary = "Lấy sách bán chạy")
    public ResponseEntity<ApiResponse<List<BookSummaryResponse>>> getBestSellingBooks(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBestSellingBooks(limit)));
    }

    @GetMapping("/suggested")
    @Operation(summary = "Lấy sách gợi ý")
    public ResponseEntity<ApiResponse<List<BookSummaryResponse>>> getSuggestedBooks(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getSuggestedBooks(limit)));
    }

    @GetMapping("/by-popular-categories")
    @Operation(summary = "Lấy sách theo danh mục phổ biến")
    public ResponseEntity<ApiResponse<List<CategoryBooksResponse>>> getBooksByPopularCategories(
            @RequestParam(required = false, defaultValue = "5") Integer categoryLimit,
            @RequestParam(required = false, defaultValue = "5") Integer bookLimit) {
        return ResponseEntity.ok(
                ApiResponse.success(bookService.getBooksByPopularCategories(categoryLimit, bookLimit)));
    }

    @GetMapping("/by-category/{categoryId}")
    @Operation(summary = "Lấy sách theo danh mục")
    public ResponseEntity<ApiResponse<PageResponse<BookSummaryResponse>>> getBooksByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBooksByCategory(categoryId, page, pageSize)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết sách")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBookById(id)));
    }

    @GetMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyBook(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bookService.verifyBook(id)));
    }

    @GetMapping("/{bookId}/exists")
    public ResponseEntity<ApiResponse<BookExistsResponse>> checkBookExists(@PathVariable UUID bookId) {
        return ResponseEntity.ok(ApiResponse.success(bookService.checkBookExists(bookId)));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<BatchBooksResponse>> getBooksBatch(@RequestBody @Valid BatchBooksRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBooksBatch(request.getIds())));
    }

    @PostMapping("/validate-ids")
    public ResponseEntity<ApiResponse<ValidateBookIdsResponse>> validateBookIds(
            @RequestBody @Valid ValidateBookIdsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookService.validateBookIds(request.getBookIds())));
    }

    @PostMapping("/batch-details")
    public ResponseEntity<ApiResponse<List<BatchBookDetailItemResponse>>> getBatchBookDetails(
            @RequestBody @Valid BatchBookDetailsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBatchBookDetails(request.getBookIds())));
    }

    @PostMapping("/reduce-stock")
    public ResponseEntity<ApiResponse<Void>> reduceStock(@RequestBody @Valid ReduceStockRequest request) {
        bookService.reduceStock(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@RequestBody @Valid CreateBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(bookService.createBook(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateBookRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookService.updateBook(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).message("Success").data(null).build());
    }
}
