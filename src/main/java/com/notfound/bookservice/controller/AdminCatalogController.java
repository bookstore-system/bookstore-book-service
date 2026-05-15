package com.notfound.bookservice.controller;

import com.notfound.bookservice.model.dto.request.AdminCreateCategoryRequest;
import com.notfound.bookservice.model.dto.request.CreateBookRequest;
import com.notfound.bookservice.model.dto.request.UpdateBookRequest;
import com.notfound.bookservice.model.dto.request.UpdateCategoryRequest;
import com.notfound.bookservice.model.dto.request.UploadAvatarRequest;
import com.notfound.bookservice.model.dto.response.ApiResponse;
import com.notfound.bookservice.model.dto.response.BookFullDetailResponse;
import com.notfound.bookservice.model.dto.response.CategoryResponse;
import com.notfound.bookservice.service.AdminCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Catalog", description = "Quản trị sách, ảnh sách và danh mục")
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    // --- Books ---

    @GetMapping("/books")
    @Operation(summary = "Lấy danh sách sách admin có phân trang")
    public ResponseEntity<ApiResponse<Page<BookFullDetailResponse>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(adminCatalogService.getAllBooks(PageRequest.of(page, size))));
    }

    @GetMapping("/books/{bookId}")
    @Operation(summary = "Lấy chi tiết sách admin")
    public ResponseEntity<ApiResponse<BookFullDetailResponse>> getBookDetail(@PathVariable UUID bookId) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.getBookDetail(bookId)));
    }

    @PostMapping("/books")
    @Operation(summary = "Tạo sách (admin)")
    public ResponseEntity<ApiResponse<BookFullDetailResponse>> createBook(
            @Valid @RequestBody CreateBookRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.createBook(request)));
    }

    @PutMapping("/books/{bookId}")
    @Operation(summary = "Cập nhật sách (admin)")
    public ResponseEntity<ApiResponse<BookFullDetailResponse>> updateBook(
            @PathVariable UUID bookId, @Valid @RequestBody UpdateBookRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.updateBook(bookId, request)));
    }

    @DeleteMapping("/books/{bookId}")
    @Operation(summary = "Xóa sách (admin)")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable UUID bookId) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.deleteBook(bookId)));
    }

    @PostMapping(value = "/books/{bookId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ảnh sách")
    public ResponseEntity<ApiResponse<BookFullDetailResponse>> uploadBookImages(
            @PathVariable UUID bookId, @RequestParam("images") List<MultipartFile> images) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.uploadBookImages(bookId, images)));
    }

    @DeleteMapping("/books/{bookId}/images/{imageId}")
    @Operation(summary = "Xóa ảnh sách")
    public ResponseEntity<ApiResponse<String>> deleteBookImage(
            @PathVariable UUID bookId, @PathVariable UUID imageId) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.deleteBookImage(bookId, imageId)));
    }

    // --- Categories ---

    @GetMapping("/categories")
    @Operation(summary = "Lấy tất cả thể loại admin")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.getAllCategories()));
    }

    @GetMapping("/categories/paged")
    @Operation(summary = "Lấy thể loại admin có phân trang")
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAllCategoriesPaged(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(adminCatalogService.getAllCategories(PageRequest.of(page, size))));
    }

    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Lấy chi tiết thể loại admin")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.getCategory(categoryId)));
    }

    @PostMapping("/categories")
    @Operation(summary = "Tạo thể loại (admin)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody AdminCreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.createCategory(request)));
    }

    @PutMapping("/categories/{categoryId}")
    @Operation(summary = "Cập nhật thể loại (admin)")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID categoryId, @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.updateCategory(categoryId, request)));
    }

    @DeleteMapping("/categories/{categoryId}")
    @Operation(summary = "Xóa thể loại (admin)")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.deleteCategory(categoryId)));
    }

    // --- Upload ---

    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar (multipart)")
    public ResponseEntity<ApiResponse<String>> uploadAvatarMultipart(@RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.uploadAvatar(image)));
    }

    @PostMapping(value = "/upload/avatar", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload avatar (JSON URL hoặc base64)")
    public ResponseEntity<ApiResponse<String>> uploadAvatarJson(@Valid @RequestBody UploadAvatarRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminCatalogService.uploadAvatarFromRequest(request.getImage())));
    }
}
