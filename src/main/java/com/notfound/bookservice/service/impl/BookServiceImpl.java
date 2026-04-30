package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.exception.ResourceNotFoundException;
import com.notfound.bookservice.model.dto.request.CreateBookRequest;
import com.notfound.bookservice.model.dto.request.UpdateBookRequest;
import com.notfound.bookservice.model.dto.response.AuthorResponse;
import com.notfound.bookservice.model.dto.response.BookResponse;
import com.notfound.bookservice.model.dto.response.BookSummaryResponse;
import com.notfound.bookservice.model.dto.response.CategoryResponse;
import com.notfound.bookservice.model.dto.response.PageResponse;
import com.notfound.bookservice.model.entity.Author;
import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.BookImage;
import com.notfound.bookservice.model.entity.Category;
import com.notfound.bookservice.model.mapper.BookMapper;
import com.notfound.bookservice.repository.AuthorRepository;
import com.notfound.bookservice.repository.BookRepository;
import com.notfound.bookservice.repository.CategoryRepository;
import com.notfound.bookservice.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    @Override
    public PageResponse<BookSummaryResponse> getBooks(Integer page, Integer size, String keyword) {
        int pageNumber = page == null || page < 0 ? 0 : page;
        int pageSize = size == null || size <= 0 ? 10 : size;
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);

        Page<Book> bookPage;
        if (StringUtils.hasText(keyword)) {
            bookPage = bookRepository.searchBooks(keyword.trim(), pageable);
        } else {
            bookPage = bookRepository.findAll(pageable);
        }
        return PageResponse.<BookSummaryResponse>builder()
                .content(bookPage.getContent().stream().map(bookMapper::toBookSummaryResponse).toList())
                .currentPage(bookPage.getNumber())
                .totalPages(bookPage.getTotalPages())
                .totalElements(bookPage.getTotalElements())
                .build();
    }

    @Override
    public BookResponse getBookById(UUID id) {
        Book book = findBookById(id);
        return bookMapper.toBookResponse(book);
    }

    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
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
        return bookMapper.toBookResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponse updateBook(UUID id, UpdateBookRequest request) {
        Book book = findBookById(id);
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
        return bookMapper.toBookResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public boolean verifyBook(UUID id) {
        return bookRepository.existsById(id);
    }

    @Override
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> CategoryResponse.builder().id(category.getId()).name(category.getName()).build())
                .toList();
    }

    @Override
    public List<AuthorResponse> getAuthors() {
        return authorRepository.findAll().stream()
                .map(author -> AuthorResponse.builder().id(author.getId()).name(author.getName()).build())
                .toList();
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
            book.setAuthors(authors);
        }

        if (categoryIds != null) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
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
}
