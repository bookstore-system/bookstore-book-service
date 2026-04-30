package com.notfound.bookservice.controller;

import com.notfound.bookservice.model.dto.request.CreateBookRequest;
import com.notfound.bookservice.model.dto.request.UpdateBookRequest;
import com.notfound.bookservice.model.dto.response.ApiResponse;
import com.notfound.bookservice.model.dto.response.BookResponse;
import com.notfound.bookservice.model.dto.response.BookSummaryResponse;
import com.notfound.bookservice.model.dto.response.PageResponse;
import com.notfound.bookservice.service.BookService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookSummaryResponse>>> getBooks(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBooks(page, size, keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBookById(id)));
    }

    @GetMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyBook(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bookService.verifyBook(id)));
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
