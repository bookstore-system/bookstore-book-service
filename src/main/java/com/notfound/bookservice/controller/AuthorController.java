package com.notfound.bookservice.controller;

import com.notfound.bookservice.model.dto.request.AuthorFilterRequest;
import com.notfound.bookservice.model.dto.request.AuthorRequest;
import com.notfound.bookservice.model.dto.request.AuthorSearchRequest;
import com.notfound.bookservice.model.dto.response.ApiResponse;
import com.notfound.bookservice.model.dto.response.AuthorResponse;
import com.notfound.bookservice.model.dto.response.PageResponse;
import com.notfound.bookservice.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Tác giả")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tác giả có phân trang")
    public ResponseEntity<ApiResponse<PageResponse<AuthorResponse>>> getAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortByName,
            @RequestParam(required = false) String sortByBirthYear) {
        return ResponseEntity.ok(ApiResponse.success(authorService.getAuthors(page, size, sortByName, sortByBirthYear)));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm tác giả theo tên")
    public ResponseEntity<ApiResponse<PageResponse<AuthorResponse>>> searchByName(
            @Valid @ModelAttribute AuthorSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authorService.searchByName(request)));
    }

    @GetMapping("/filter")
    @Operation(summary = "Lọc tác giả")
    public ResponseEntity<ApiResponse<PageResponse<AuthorResponse>>> filterAuthors(
            @Valid @ModelAttribute AuthorFilterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authorService.filterAuthors(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết tác giả")
    public ResponseEntity<ApiResponse<AuthorResponse>> getAuthorById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(authorService.getAuthorById(id)));
    }

    @PostMapping
    @Operation(summary = "Tạo tác giả mới")
    public ResponseEntity<ApiResponse<AuthorResponse>> createAuthor(@Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authorService.createAuthor(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tác giả")
    public ResponseEntity<ApiResponse<AuthorResponse>> updateAuthor(
            @PathVariable UUID id, @Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authorService.updateAuthor(id, request)));
    }
}
