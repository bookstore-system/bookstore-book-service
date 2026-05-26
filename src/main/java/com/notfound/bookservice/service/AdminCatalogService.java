package com.notfound.bookservice.service;

import com.notfound.bookservice.model.dto.request.AdminCreateCategoryRequest;
import com.notfound.bookservice.model.dto.request.CreateBookRequest;
import com.notfound.bookservice.model.dto.request.UpdateBookRequest;
import com.notfound.bookservice.model.dto.request.UpdateCategoryRequest;
import com.notfound.bookservice.model.dto.response.BookFullDetailResponse;
import com.notfound.bookservice.model.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AdminCatalogService {

    BookFullDetailResponse createBook(CreateBookRequest request);

    BookFullDetailResponse updateBook(UUID bookId, UpdateBookRequest request);

    String deleteBook(UUID bookId);

    BookFullDetailResponse getBookDetail(UUID bookId);

    Page<BookFullDetailResponse> getAllBooks(Pageable pageable);

    BookFullDetailResponse uploadBookImages(UUID bookId, List<MultipartFile> images);

    String deleteBookImage(UUID bookId, UUID imageId);

    CategoryResponse createCategory(AdminCreateCategoryRequest request);

    CategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request);

    String deleteCategory(UUID categoryId);

    CategoryResponse getCategory(UUID categoryId);

    List<CategoryResponse> getAllCategories();

    Page<CategoryResponse> getAllCategories(Pageable pageable);

    String uploadAvatar(MultipartFile image);

    String uploadAvatarFromRequest(String imagePayload);
}
