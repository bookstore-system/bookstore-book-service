package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.exception.ResourceNotFoundException;
import com.notfound.bookservice.model.dto.request.AdminCreateCategoryRequest;
import com.notfound.bookservice.model.dto.request.CreateBookRequest;
import com.notfound.bookservice.model.dto.request.UpdateBookRequest;
import com.notfound.bookservice.model.dto.request.UpdateCategoryRequest;
import com.notfound.bookservice.model.dto.response.BookFullDetailResponse;
import com.notfound.bookservice.model.dto.response.CategoryResponse;
import com.notfound.bookservice.model.entity.Author;
import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.BookImage;
import com.notfound.bookservice.model.entity.Category;
import com.notfound.bookservice.repository.AuthorRepository;
import com.notfound.bookservice.repository.BookImageRepository;
import com.notfound.bookservice.repository.BookRepository;
import com.notfound.bookservice.repository.CategoryRepository;
import com.notfound.bookservice.service.AdminCatalogService;
import com.notfound.bookservice.service.GeminiService;
import com.notfound.bookservice.service.ImageService;
import com.notfound.bookservice.service.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCatalogServiceImpl implements AdminCatalogService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final BookImageRepository bookImageRepository;
    private final ImageService imageService;
    private final GeminiService geminiService;
    private final QdrantService qdrantService;

    @Override
    @Transactional
    public BookFullDetailResponse createBook(CreateBookRequest request) {
        validateIsbnUnique(request.getIsbn(), null);
        Book book = Book.builder()
                .title(request.getTitle().trim())
                .isbn(StringUtils.hasText(request.getIsbn()) ? request.getIsbn().trim() : null)
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .importPrice(request.getImportPrice())
                .stockQuantity(request.getStockQuantity())
                .publishDate(request.getPublishDate())
                .description(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null)
                .status(parseStatus(request.getStatus()))
                .build();
        applyRelations(book, request.getAuthorIds(), request.getCategoryIds(), request.getImageUrls());
        book = bookRepository.save(book);
        indexBookInQdrant(book);
        return mapToBookFullDetail(book);
    }

    @Override
    @Transactional
    public BookFullDetailResponse updateBook(UUID bookId, UpdateBookRequest request) {
        Book book = findBook(bookId);
        if (StringUtils.hasText(request.getIsbn())) {
            validateIsbnUnique(request.getIsbn(), bookId);
            book.setIsbn(request.getIsbn().trim());
        }
        if (request.getTitle() != null) book.setTitle(request.getTitle().trim());
        if (request.getPrice() != null) book.setPrice(request.getPrice());
        if (request.getDiscountPrice() != null) book.setDiscountPrice(request.getDiscountPrice());
        if (request.getImportPrice() != null) book.setImportPrice(request.getImportPrice());
        if (request.getStockQuantity() != null) book.setStockQuantity(request.getStockQuantity());
        if (request.getPublishDate() != null) book.setPublishDate(request.getPublishDate());
        if (request.getDescription() != null) {
            book.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        }
        if (request.getStatus() != null) book.setStatus(parseStatus(request.getStatus()));
        applyRelations(book, request.getAuthorIds(), request.getCategoryIds(), request.getImageUrls());
        return mapToBookFullDetail(bookRepository.save(book));
    }

    @Override
    @Transactional
    public String deleteBook(UUID bookId) {
        Book book = findBook(bookId);
        List<BookImage> images = bookImageRepository.findByBook_IdOrderByPriorityAscUploadedAtAsc(bookId);
        for (BookImage image : images) {
            if (StringUtils.hasText(image.getUrl())) {
                imageService.deleteImage(image.getUrl());
            }
        }
        bookRepository.delete(book);
        return "Xóa sách thành công";
    }

    @Override
    @Transactional(readOnly = true)
    public BookFullDetailResponse getBookDetail(UUID bookId) {
        return mapToBookFullDetail(findBook(bookId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookFullDetailResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::mapToBookFullDetail);
    }

    @Override
    @Transactional
    public BookFullDetailResponse uploadBookImages(UUID bookId, List<MultipartFile> images) {
        Book book = findBook(bookId);
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("Danh sách ảnh không được rỗng");
        }
        List<Map<String, Object>> uploads = imageService.uploadMultipleImages(images, "bookstore/books");
        int nextPriority = nextImagePriority(bookId);
        for (Map<String, Object> result : uploads) {
            String url = (String) result.get("url");
            bookImageRepository.save(BookImage.builder()
                    .book(book)
                    .url(url)
                    .priority(nextPriority++)
                    .build());
        }
        return mapToBookFullDetail(findBook(bookId));
    }

    @Override
    @Transactional
    public String deleteBookImage(UUID bookId, UUID imageId) {
        BookImage image = bookImageRepository
                .findByIdAndBook_Id(imageId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        if (StringUtils.hasText(image.getUrl())) {
            imageService.deleteImage(image.getUrl());
        }
        bookImageRepository.delete(image);
        return "Xóa ảnh thành công";
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(AdminCreateCategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tên thể loại đã tồn tại: " + name);
        }
        Category saved = categoryRepository.save(Category.builder()
                .name(name)
                .description(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null)
                .build());
        return mapToCategoryResponse(saved, loadBookCountsPerCategory());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request) {
        Category category = findCategory(categoryId);
        if (StringUtils.hasText(request.getName())) {
            String name = request.getName().trim();
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, categoryId)) {
                throw new IllegalArgumentException("Tên thể loại đã tồn tại: " + name);
            }
            category.setName(name);
        }
        if (request.getDescription() != null) {
            category.setDescription(
                    StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        }
        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId().isBlank()) {
                category.setParentCategory(null);
            } else {
                UUID parentId = UUID.fromString(request.getParentCategoryId());
                if (parentId.equals(categoryId)) {
                    throw new IllegalArgumentException("Thể loại không thể là cha của chính nó");
                }
                Category parent = categoryRepository
                        .findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Parent category not found with id: " + parentId));
                category.setParentCategory(parent);
            }
        }
        return mapToCategoryResponse(categoryRepository.save(category), loadBookCountsPerCategory());
    }

    @Override
    @Transactional
    public String deleteCategory(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        if (categoryRepository.hasBooks(categoryId)) {
            throw new IllegalArgumentException("Không thể xóa thể loại đang có sách");
        }
        categoryRepository.deleteById(categoryId);
        return "Xóa thể loại thành công";
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID categoryId) {
        Category category = categoryRepository
                .findDetailedById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return mapToCategoryResponse(category, loadBookCountsPerCategory());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        Map<UUID, Long> bookCounts = loadBookCountsPerCategory();
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(category -> mapToCategoryResponse(category, bookCounts))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        Map<UUID, Long> bookCounts = loadBookCountsPerCategory();
        return categoryRepository.findAll(pageable).map(category -> mapToCategoryResponse(category, bookCounts));
    }

    @Override
    public String uploadAvatar(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không hợp lệ");
        }
        Map<String, Object> result = imageService.uploadImage(image, "bookstore/avatars");
        return (String) result.get("url");
    }

    @Override
    public String uploadAvatarFromRequest(String imagePayload) {
        if (!StringUtils.hasText(imagePayload)) {
            throw new IllegalArgumentException("image không được để trống");
        }
        String trimmed = imagePayload.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        byte[] bytes = decodeBase64Image(trimmed);
        Map<String, Object> result = imageService.uploadImageBytes(bytes, "bookstore/avatars");
        return (String) result.get("url");
    }

    private byte[] decodeBase64Image(String payload) {
        try {
            String base64 = payload;
            if (payload.contains(",")) {
                base64 = payload.split(",", 2)[1];
            }
            return Base64.getDecoder().decode(base64);
        } catch (Exception e) {
            throw new IllegalArgumentException("image không phải URL hoặc base64 hợp lệ");
        }
    }

    private void indexBookInQdrant(Book book) {
        String embeddingText = book.getTitle() + ". "
                + (book.getDescription() != null ? book.getDescription() : "");
        log.info("Creating embedding for book: {}", book.getId());

        try {
            double[] vector = geminiService.embed(embeddingText);
            qdrantService.insertBookVector(book.getId(), vector, book);
            log.info("Inserted vector to Qdrant for book: {}", book.getId());
        } catch (Exception e) {
            log.error("Failed to index book {} in Qdrant: {}", book.getId(), e.getMessage(), e);
        }
    }

    private Book findBook(UUID bookId) {
        return bookRepository
                .findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
    }

    private Category findCategory(UUID categoryId) {
        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    private void validateIsbnUnique(String isbn, UUID excludeId) {
        if (!StringUtils.hasText(isbn)) {
            return;
        }
        String normalized = isbn.trim();
        boolean exists = excludeId == null
                ? bookRepository.existsByIsbn(normalized)
                : bookRepository.existsByIsbnAndIdNot(normalized, excludeId);
        if (exists) {
            throw new IllegalArgumentException("isbn already exists");
        }
    }

    private Book.Status parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return Book.Status.AVAILABLE;
        }
        return Book.Status.valueOf(status.trim().toUpperCase());
    }

    private void applyRelations(
            Book book, List<UUID> authorIds, List<UUID> categoryIds, List<String> imageUrls) {
        if (authorIds != null) {
            List<Author> authors = authorRepository.findAllById(authorIds);
            if (authors.size() != new HashSet<>(authorIds).size()) {
                throw new IllegalArgumentException("One or more authorIds do not exist");
            }
            book.setAuthors(authors);
        }
        if (categoryIds != null) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            if (categories.size() != new HashSet<>(categoryIds).size()) {
                throw new IllegalArgumentException("One or more categoryIds do not exist");
            }
            book.setCategories(categories);
        }
        if (imageUrls != null) {
            int priority = nextImagePriority(book.getId());
            List<BookImage> images = new ArrayList<>();
            for (String url : imageUrls) {
                if (StringUtils.hasText(url)) {
                    images.add(BookImage.builder()
                            .url(url.trim())
                            .book(book)
                            .priority(priority++)
                            .build());
                }
            }
            book.getImages().clear();
            book.getImages().addAll(images);
        }
    }

    private int nextImagePriority(UUID bookId) {
        return bookImageRepository.findByBook_IdOrderByPriorityAscUploadedAtAsc(bookId).stream()
                        .mapToInt(img -> img.getPriority() != null ? img.getPriority() : 0)
                        .max()
                        .orElse(0)
                + 1;
    }

    private BookFullDetailResponse mapToBookFullDetail(Book book) {
        List<BookFullDetailResponse.AuthorInfo> authors = book.getAuthors() == null
                ? List.of()
                : book.getAuthors().stream()
                        .map(author -> BookFullDetailResponse.AuthorInfo.builder()
                                .id(author.getId())
                                .name(author.getName())
                                .biography(author.getBiography())
                                .dateOfBirth(author.getDateOfBirth())
                                .nationality(author.getNationality())
                                .build())
                        .toList();

        List<BookFullDetailResponse.CategoryInfo> categories = book.getCategories() == null
                ? List.of()
                : book.getCategories().stream()
                        .map(category -> BookFullDetailResponse.CategoryInfo.builder()
                                .id(category.getId())
                                .name(category.getName())
                                .description(category.getDescription())
                                .build())
                        .toList();

        List<BookImage> imageEntities =
                bookImageRepository.findByBook_IdOrderByPriorityAscUploadedAtAsc(book.getId());
        List<BookFullDetailResponse.ImageInfo> images = imageEntities.stream()
                .map(image -> BookFullDetailResponse.ImageInfo.builder()
                        .id(image.getId())
                        .url(image.getUrl())
                        .priority(image.getPriority())
                        .uploadedAt(image.getUploadedAt())
                        .build())
                .toList();

        return BookFullDetailResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .discountPrice(book.getDiscountPrice())
                .importPrice(book.getImportPrice())
                .stockQuantity(book.getStockQuantity())
                .publishDate(book.getPublishDate())
                .description(book.getDescription())
                .status(book.getStatus().name())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .createdBy(book.getCreatedBy())
                .updatedBy(book.getUpdatedBy())
                .authors(authors)
                .categories(categories)
                .images(images)
                .averageRating(0.0)
                .totalReviews(0)
                .totalOrders(0)
                .build();
    }

    private Map<UUID, Long> loadBookCountsPerCategory() {
        return categoryRepository.countBooksPerCategory().stream()
                .collect(Collectors.toMap(
                        CategoryRepository.CategoryBookCountProjection::getCategoryId,
                        CategoryRepository.CategoryBookCountProjection::getBookCount));
    }

    private CategoryResponse mapToCategoryResponse(Category category, Map<UUID, Long> bookCounts) {
        CategoryResponse.CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .totalBooks(bookCounts.getOrDefault(category.getId(), 0L).intValue());

        if (category.getParentCategory() != null) {
            builder.parentCategoryId(category.getParentCategory().getId())
                    .parentCategoryName(category.getParentCategory().getName());
        }
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            builder.subCategories(category.getSubCategories().stream()
                    .map(Category::getName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList());
        }
        return builder.build();
    }
}
