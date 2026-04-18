package service;

import dto.response.bookresponse.BookResponse;
import dto.response.bookresponse.BookSummaryResponse;
import dto.response.bookresponse.PageResponse;

public interface BookService {
    PageResponse<BookSummaryResponse> getAllBooks(int page, int size);
    BookResponse getBookById(String id);
    BookResponse createBook(dto.request.bookrequest.bookrequest.CreateBookRequest request);
    BookResponse updateBook(String id, dto.request.bookrequest.bookrequest.UpdateBookRequest request);
    void deleteBook(String id);
}
