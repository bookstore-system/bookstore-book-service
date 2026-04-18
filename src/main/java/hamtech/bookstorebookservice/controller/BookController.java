package hamtech.bookstorebookservice.controller;

import dto.response.ApiResponse;
import dto.request.bookrequest.bookrequest.CreateBookRequest;
import dto.request.bookrequest.bookrequest.UpdateBookRequest;
import dto.response.bookresponse.BookResponse;
import dto.response.bookresponse.BookSummaryResponse;
import dto.response.bookresponse.PageResponse;
import hamtech.bookstorebookservice.entity.Book;
import hamtech.bookstorebookservice.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import hamtech.bookstorebookservice.repository.BookRepository;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;

    @Autowired
    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@RequestBody CreateBookRequest req) {
        Book book = new Book();
        book.setTitle(req.getTitle());
        book.setIsbn(req.getIsbn());
        book.setPrice(req.getPrice());
        book.setDiscountPrice(req.getDiscountPrice());
        book.setImportPrice(req.getImportPrice());
        book.setStockQuantity(req.getStockQuantity());
        book.setPublishDate(req.getPublishDate());
        book.setDescription(req.getDescription());

        Book saved = bookRepository.save(book);

        BookResponse resp = toBookResponse(saved);

        ApiResponse<BookResponse> api = ApiResponse.<BookResponse>builder()
                .code(1000)
                .message("Created")
                .result(resp)
                .build();

        return ResponseEntity.ok(api);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        Book book = bookRepository.findById(uuid).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();
        BookResponse resp = toBookResponse(book);
        ApiResponse<BookResponse> api = ApiResponse.<BookResponse>builder()
                .code(1000)
                .message("OK")
                .result(resp)
                .build();
        return ResponseEntity.ok(api);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(@PathVariable String id, @RequestBody UpdateBookRequest req) {
        UUID uuid;
        try { uuid = UUID.fromString(id);} catch (Exception e){ return ResponseEntity.badRequest().build(); }
        Book book = bookRepository.findById(uuid).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();

        if (req.getTitle() != null) book.setTitle(req.getTitle());
        if (req.getIsbn() != null) book.setIsbn(req.getIsbn());
        if (req.getPrice() != null) book.setPrice(req.getPrice());
        if (req.getDiscountPrice() != null) book.setDiscountPrice(req.getDiscountPrice());
        if (req.getImportPrice() != null) book.setImportPrice(req.getImportPrice());
        if (req.getStockQuantity() != null) book.setStockQuantity(req.getStockQuantity());
        if (req.getPublishDate() != null) book.setPublishDate(req.getPublishDate());
        if (req.getDescription() != null) book.setDescription(req.getDescription());

        Book saved = bookRepository.save(book);
        BookResponse resp = toBookResponse(saved);
        ApiResponse<BookResponse> api = ApiResponse.<BookResponse>builder()
                .code(1000)
                .message("Updated")
                .result(resp)
                .build();
        return ResponseEntity.ok(api);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String id) {
        UUID uuid;
        try { uuid = UUID.fromString(id);} catch (Exception e){ return ResponseEntity.badRequest().build(); }
        boolean exists = bookRepository.existsById(uuid);
        if (!exists) return ResponseEntity.notFound().build();
        bookRepository.deleteById(uuid);
        ApiResponse<Void> api = ApiResponse.<Void>builder()
                .code(1000)
                .message("Deleted")
                .result(null)
                .build();
        return ResponseEntity.ok(api);
    }

        @GetMapping
        public ResponseEntity<ApiResponse<List<BookSummaryResponse>>> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        List<BookSummaryResponse> resp = books.stream().map(this::toBookSummary).collect(Collectors.toList());
        ApiResponse<List<BookSummaryResponse>> api = ApiResponse.<List<BookSummaryResponse>>builder()
            .code(1000)
            .message("OK")
            .result(resp)
            .build();
        return ResponseEntity.ok(api);
        }

    // --- simple mappers ---
    private BookResponse toBookResponse(Book b) {
        BookResponse r = new BookResponse();
        r.setId(b.getId() != null ? b.getId().toString() : null);
        r.setTitle(b.getTitle());
        r.setIsbn(b.getIsbn());
        r.setPrice(b.getPrice());
        r.setImportPrice(b.getImportPrice());
        r.setDiscountPrice(b.getDiscountPrice());
        r.setStockQuantity(b.getStockQuantity());
        r.setPublishDate(b.getPublishDate());
        r.setDescription(b.getDescription());
        // authors/categories/images not populated in simple CRUD
        return r;
    }

    private BookSummaryResponse toBookSummary(Book b) {
        BookSummaryResponse s = new BookSummaryResponse();
        s.setId(b.getId());
        s.setTitle(b.getTitle());
        s.setPrice(b.getPrice());
        s.setDiscountPrice(b.getDiscountPrice());
        s.setMainImageUrl(null);
        s.setStockQuantity(b.getStockQuantity());
        s.setAuthorNames(null);
        s.setCategoryId(null);
        return s;
    }
}
