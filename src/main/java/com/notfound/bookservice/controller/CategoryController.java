package com.notfound.bookservice.controller;

import com.notfound.bookservice.model.dto.request.CreateCategoryRequest;
import com.notfound.bookservice.model.dto.response.ApiResponse;
import com.notfound.bookservice.model.dto.response.CategoryResponse;
import com.notfound.bookservice.model.dto.response.CategoryWithBookResponse;
import com.notfound.bookservice.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Danh mục sách")
public class CategoryController {
    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Lấy tất cả thể loại")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(bookService.getCategories()));
    }

    @GetMapping("/with-sample-book")
    @Operation(summary = "Lấy thể loại kèm sách mẫu")
    public ResponseEntity<ApiResponse<List<CategoryWithBookResponse>>> getCategoriesWithSampleBook() {
        return ResponseEntity.ok(ApiResponse.success(bookService.getCategoriesWithSampleBook()));
    }

    @GetMapping("/popular")
    @Operation(summary = "Lấy thể loại phổ biến")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getPopularCategories(
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getPopularCategories(limit)));
    }

    @GetMapping("/paged")
    @Operation(summary = "Lấy thể loại có phân trang")
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getCategoriesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getCategoriesPaged(page, size)));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Lấy chi tiết thể loại")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getCategoryById(categoryId)));
    }

    @PostMapping
    @Operation(summary = "Tạo thể loại mới")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookService.createCategory(request)));
    }
}
