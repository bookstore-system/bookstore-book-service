package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.exception.ResourceNotFoundException;
import com.notfound.bookservice.model.dto.request.BookFilterRequest;
import com.notfound.bookservice.model.dto.request.BookOptionsRequest;
import com.notfound.bookservice.model.dto.request.BookSortRequest;
import com.notfound.bookservice.model.dto.request.CreateBookRequest;
import com.notfound.bookservice.model.dto.request.CreateCategoryRequest;
import com.notfound.bookservice.model.dto.request.ReduceStockItemRequest;
import com.notfound.bookservice.model.dto.request.ReduceStockRequest;
import com.notfound.bookservice.model.dto.request.UpdateBookRequest;
import com.notfound.bookservice.model.dto.response.BatchBookDetailItemResponse;
import com.notfound.bookservice.model.dto.response.BatchBookItemResponse;
import com.notfound.bookservice.model.dto.response.BatchBooksResponse;
import com.notfound.bookservice.model.dto.response.BookResponse;
import com.notfound.bookservice.model.dto.response.BookExistsResponse;
import com.notfound.bookservice.model.dto.response.BookSummaryResponse;
import com.notfound.bookservice.model.dto.response.CategoryBooksResponse;
import com.notfound.bookservice.model.dto.response.CategoryResponse;
import com.notfound.bookservice.model.dto.response.CategoryWithBookResponse;
import com.notfound.bookservice.model.dto.response.PageResponse;
import com.notfound.bookservice.model.dto.response.ValidateBookIdsResponse;
import com.notfound.bookservice.model.entity.Author;
import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.BookImage;
import com.notfound.bookservice.model.entity.Category;
import com.notfound.bookservice.model.mapper.BookMapper;
import com.notfound.bookservice.repository.AuthorRepository;
import com.notfound.bookservice.repository.BookRepository;
import com.notfound.bookservice.repository.CategoryRepository;
import com.notfound.bookservice.service.BookService;
import com.notfound.bookservice.service.BookVectorSyncService;
import com.notfound.bookservice.service.GeminiService;
import com.notfound.bookservice.service.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final GeminiService geminiService;
    private final QdrantService qdrantService;
    private final BookVectorSyncService bookVectorSyncService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookSummaryResponse> getBooks(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(defaultPage(page), defaultSize(size));
        return toPageResponse(bookRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookSummaryResponse> searchBooks(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(defaultPage(page), defaultSize(size));

        if (!StringUtils.hasText(keyword)) {
            return toPageResponse(bookRepository.findAll(pageable));
        }

        String searchKeyword = keyword.trim();
        log.info("AI searching books with keyword: {}", searchKeyword);

        try {
            double[] queryVector = geminiService.embed(searchKeyword);
            List<String> bookIds = qdrantService.searchBookIds(queryVector, 50);

            if (!bookIds.isEmpty()) {
                List<UUID> uuidList = bookIds.stream().map(UUID::fromString).toList();
                List<Book> dbBooks = bookRepository.findAllById(uuidList);
                Map<UUID, Book> bookMap = dbBooks.stream()
                        .collect(Collectors.toMap(Book::getId, Function.identity()));

                List<Book> orderedBooks = uuidList.stream()
                        .map(bookMap::get)
                        .filter(Objects::nonNull)
                        .toList();

                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), orderedBooks.size());
                List<Book> pagedBooks = start >= orderedBooks.size()
                        ? List.of()
                        : orderedBooks.subList(start, end);

                Page<Book> bookPage = new PageImpl<>(pagedBooks, pageable, orderedBooks.size());
                return toPageResponse(bookPage);
            }
        } catch (Exception e) {
            log.warn("AI vector search failed, falling back to database search: {}", e.getMessage());
        }

        log.warn("AI search empty or unavailable, using database search");
        return toPageResponse(bookRepository.searchBooks(searchKeyword, pageable));
    }

    @Override
    public PageResponse<BookSummaryResponse> getAllBooksOption(BookOptionsRequest request) {
        Sort sort = switch (request.getOption() == null ? "phobien" : request.getOption()) {
            case "moinhat" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "thapdencao" -> Sort.by(Sort.Direction.ASC, "discountPrice");
            case "caodenthap", "phobien", "danhgiacao" -> Sort.by(Sort.Direction.DESC, "discountPrice");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(defaultPage(request.getPage()), defaultSize(request.getSize()), sort);
        List<UUID> resolvedCategoryIds = request.resolveCategoryIds();
        UUID categoryId = resolvedCategoryIds.isEmpty() ? null : resolvedCategoryIds.get(0);
        Page<Book> page = bookRepository.findByFilters(null, request.getMinPrice(), request.getMaxPrice(), null, categoryId, pageable);
        return toPageResponse(page);
    }

    @Override
    public PageResponse<BookSummaryResponse> findByFilters(BookFilterRequest request) {
        Pageable pageable = PageRequest.of(defaultPage(request.getPage()), defaultSize(request.getSize()));
        Page<Book> page = bookRepository.findByFilters(
                normalizeKeyword(request.getKeyword()),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getPublishedAfter(),
                request.getCategoryId(),
                pageable
        );
        return toPageResponse(page);
    }

    @Override
    public PageResponse<BookSummaryResponse> getSortedBooks(BookSortRequest request) {
        Sort sort = mapSort(request.getSortType());
        Pageable pageable = PageRequest.of(defaultPage(request.getPage()), defaultSize(request.getSize()), sort);
        return toPageResponse(bookRepository.findAll(pageable));
    }

    @Override
    public List<BookSummaryResponse> getBestSellingBooks(Integer limit) {
        int safeLimit = (limit == null || limit < 1) ? 10 : Math.min(limit, 50);
        return bookRepository.findBestSellingBooks(PageRequest.of(0, safeLimit)).stream()
                .map(bookMapper::toBookSummaryResponse)
                .toList();
    }

    @Override
    public List<BookSummaryResponse> getSuggestedBooks(Integer limit) {
        int safeLimit = (limit == null || limit < 1) ? 10 : Math.min(limit, 50);
        return bookRepository.findRandomBooks(PageRequest.of(0, safeLimit)).stream()
                .map(bookMapper::toBookSummaryResponse)
                .toList();
    }

    @Override
    public List<CategoryBooksResponse> getBooksByPopularCategories(Integer categoryLimit, Integer bookLimit) {
        int safeCategoryLimit = (categoryLimit == null || categoryLimit < 1) ? 5 : Math.min(categoryLimit, 20);
        int safeBookLimit = (bookLimit == null || bookLimit < 1) ? 5 : Math.min(bookLimit, 20);
        List<Category> categories = categoryRepository.findPopularCategories(PageRequest.of(0, safeCategoryLimit));
        Map<UUID, Long> bookCounts = loadBookCountsPerCategory();

        return categories.stream().map(category -> {
            List<BookSummaryResponse> books = bookRepository
                    .findByCategoryId(category.getId(), PageRequest.of(0, safeBookLimit))
                    .getContent()
                    .stream()
                    .map(bookMapper::toBookSummaryResponse)
                    .toList();
            return new CategoryBooksResponse(mapToCategoryResponse(category, bookCounts), books);
        }).toList();
    }

    @Override
    public PageResponse<BookSummaryResponse> getBooksByCategory(UUID categoryId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(defaultPage(page), defaultSize(pageSize));
        return toPageResponse(bookRepository.findByCategoryId(categoryId, pageable));
    }

    @Override
    public BookResponse getBookById(UUID id) {
        Book book = findBookById(id);
        return bookMapper.toBookResponse(book);
    }

    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        validateCreateRequest(request);
        Book book = Book.builder()
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .importPrice(request.getImportPrice())
                .stockQuantity(request.getStockQuantity())
                .publishDate(request.getPublishDate())
                .description(request.getDescription())
                .status(parseStatus(request.getStatus()))
                .build();

        applyRelations(book, request.getAuthorIds(), request.getCategoryIds(), request.getImageUrls());
        book = bookRepository.save(book);
        bookVectorSyncService.index(book);
        return bookMapper.toBookResponse(book);
    }

    @Override
    @Transactional
    public BookResponse updateBook(UUID id, UpdateBookRequest request) {
        Book book = findBookById(id);
        validateUpdateRequest(id, request);
        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getIsbn() != null) book.setIsbn(request.getIsbn());
        if (request.getPrice() != null) book.setPrice(request.getPrice());
        if (request.getDiscountPrice() != null) book.setDiscountPrice(request.getDiscountPrice());
        if (request.getImportPrice() != null) book.setImportPrice(request.getImportPrice());
        if (request.getStockQuantity() != null) book.setStockQuantity(request.getStockQuantity());
        if (request.getPublishDate() != null) book.setPublishDate(request.getPublishDate());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getStatus() != null) book.setStatus(parseStatus(request.getStatus()));

        applyRelations(book, request.getAuthorIds(), request.getCategoryIds(), request.getImageUrls());
        book = bookRepository.save(book);
        bookVectorSyncService.index(book);
        return bookMapper.toBookResponse(book);
    }

    @Override
    @Transactional
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        bookVectorSyncService.remove(id);
    }

    @Override
    public boolean verifyBook(UUID id) {
        return bookRepository.existsById(id);
    }

    @Override
    public BatchBooksResponse getBooksBatch(List<UUID> ids) {
        List<UUID> requestIds = ids == null ? List.of() : ids;
        List<Book> books = requestIds.isEmpty() ? List.of() : bookRepository.findAllById(requestIds);

        Map<UUID, Book> booksById = books.stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        List<BatchBookItemResponse> items = requestIds.stream()
                .map(booksById::get)
                .filter(book -> book != null)
                .map(book -> BatchBookItemResponse.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .price(book.getPrice())
                        .discountPrice(book.getDiscountPrice())
                        .thumbnailUrl(getThumbnailUrl(book))
                        .inStock(book.getStockQuantity() != null && book.getStockQuantity() > 0)
                        .build())
                .toList();

        Set<UUID> existingIds = booksById.keySet();
        List<UUID> missingIds = requestIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        return BatchBooksResponse.builder()
                .items(items)
                .missingIds(missingIds)
                .build();
    }

    @Override
    public BookExistsResponse checkBookExists(UUID id) {
        return bookRepository.findById(id)
                .map(book -> BookExistsResponse.builder()
                        .exists(true)
                        .bookId(book.getId())
                        .title(book.getTitle())
                        .build())
                .orElse(BookExistsResponse.builder()
                        .exists(false)
                        .bookId(id)
                        .title(null)
                        .build());
    }

    @Override
    public ValidateBookIdsResponse validateBookIds(List<UUID> bookIds) {
        List<UUID> requestIds = bookIds == null ? List.of() : bookIds;
        List<Book> books = requestIds.isEmpty() ? List.of() : bookRepository.findAllById(requestIds);
        Set<UUID> existingIds = books.stream().map(Book::getId).collect(Collectors.toCollection(HashSet::new));
        List<UUID> invalidBookIds = requestIds.stream()
                .filter(id -> !existingIds.contains(id))
                .distinct()
                .toList();

        return ValidateBookIdsResponse.builder()
                .allValid(invalidBookIds.isEmpty())
                .invalidBookIds(invalidBookIds)
                .build();
    }

    @Override
    public List<BatchBookDetailItemResponse> getBatchBookDetails(List<UUID> bookIds) {
        List<UUID> requestIds = bookIds == null ? List.of() : bookIds;
        List<Book> books = requestIds.isEmpty() ? List.of() : bookRepository.findAllById(requestIds);
        Map<UUID, Book> booksById = books.stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        return requestIds.stream()
                .map(booksById::get)
                .filter(book -> book != null)
                .map(book -> BatchBookDetailItemResponse.builder()
                        .bookId(book.getId())
                        .title(book.getTitle())
                        .price(book.getPrice())
                        .salePrice(book.getDiscountPrice())
                        .stockQuantity(book.getStockQuantity())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void reduceStock(ReduceStockRequest request) {
        List<ReduceStockItemRequest> items = request.getItems();
        for (ReduceStockItemRequest item : items) {
            int updated = bookRepository.decreaseStockIfEnough(item.getBookId(), item.getQuantity());
            if (updated == 0) {
                if (!bookRepository.existsById(item.getBookId())) {
                    throw new ResourceNotFoundException("Book not found with id: " + item.getBookId());
                }
                throw new IllegalArgumentException("Book out of stock for id: " + item.getBookId());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        Map<UUID, Long> bookCounts = loadBookCountsPerCategory();
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(category -> mapToCategoryResponse(category, bookCounts))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID categoryId) {
        Category category = categoryRepository
                .findDetailedById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        Map<UUID, Long> bookCounts = loadBookCountsPerCategory();
        return mapToCategoryResponse(category, bookCounts);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategoriesPaged(int page, int size) {
        Pageable pageable = PageRequest.of(defaultPage(page), defaultSize(size));
        Map<UUID, Long> bookCounts = loadBookCountsPerCategory();
        return categoryRepository.findAll(pageable).map(category -> mapToCategoryResponse(category, bookCounts));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getPopularCategories(Integer limit) {
        int safeLimit = (limit == null || limit < 1) ? 5 : Math.min(limit, 50);
        Map<UUID, Long> bookCounts = loadBookCountsPerCategory();
        return categoryRepository.findPopularCategories(PageRequest.of(0, safeLimit)).stream()
                .map(category -> mapToCategoryResponse(category, bookCounts))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryWithBookResponse> getCategoriesWithSampleBook() {
        List<Category> categories = categoryRepository.findAllWithHierarchy();
        return categories.stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(category -> {
                    CategoryWithBookResponse.CategoryWithBookResponseBuilder builder =
                            CategoryWithBookResponse.builder()
                                    .id(category.getId())
                                    .name(category.getName())
                                    .description(category.getDescription());
                    bookRepository
                            .findByCategoryId(category.getId(), PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .ifPresent(book -> builder.sampleBook(bookMapper.toBookResponse(book)));
                    return builder.build();
                })
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tên thể loại đã tồn tại: " + name);
        }
        Category parent = null;
        if (request.getParentCategoryId() != null) {
            parent = categoryRepository
                    .findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy thể loại cha với id: " + request.getParentCategoryId()));
        }
        String description = StringUtils.hasText(request.getDescription())
                ? request.getDescription().trim()
                : null;
        Category saved = categoryRepository.save(
                Category.builder().name(name).description(description).parentCategory(parent).build());
        return mapToCategoryResponse(saved, loadBookCountsPerCategory());
    }

    private Book findBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    private Book.Status parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return Book.Status.AVAILABLE;
        }
        return Book.Status.valueOf(status.trim().toUpperCase());
    }

    private void applyRelations(Book book, List<UUID> authorIds, List<UUID> categoryIds, List<String> imageUrls) {
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
            List<BookImage> images = new ArrayList<>();
            for (String url : imageUrls) {
                if (StringUtils.hasText(url)) {
                    images.add(BookImage.builder().url(url).book(book).build());
                }
            }
            book.getImages().clear();
            book.getImages().addAll(images);
        }
    }

    private String getThumbnailUrl(Book book) {
        if (book.getImages() == null || book.getImages().isEmpty()) {
            return null;
        }
        return book.getImages().get(0).getUrl();
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

    private PageResponse<BookSummaryResponse> toPageResponse(Page<Book> page) {
        return PageResponse.<BookSummaryResponse>builder()
                .content(page.getContent().stream().map(bookMapper::toBookSummaryResponse).toList())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    private int defaultPage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int defaultSize(Integer size) {
        return size == null || size <= 0 ? 10 : Math.min(size, 100);
    }

    private Sort mapSort(String sortType) {
        String safeSortType = (sortType == null || sortType.isBlank()) ? "date_desc" : sortType;
        return switch (safeSortType) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "discountPrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "discountPrice");
            case "title_asc" -> Sort.by(Sort.Direction.ASC, "title");
            case "title_desc" -> Sort.by(Sort.Direction.DESC, "title");
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "publishDate");
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "publishDate");
            default -> throw new IllegalArgumentException("Invalid sort type");
        };
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private void validateCreateRequest(CreateBookRequest request) {
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("price must be greater than 0");
        }
        if (!StringUtils.hasText(request.getIsbn())) {
            throw new IllegalArgumentException("isbn is required");
        }
        if (bookRepository.existsByIsbn(request.getIsbn().trim())) {
            throw new IllegalArgumentException("isbn already exists");
        }
    }

    private void validateUpdateRequest(UUID id, UpdateBookRequest request) {
        if (StringUtils.hasText(request.getIsbn()) && bookRepository.existsByIsbnAndIdNot(request.getIsbn().trim(), id)) {
            throw new IllegalArgumentException("isbn already exists");
        }
    }
}
