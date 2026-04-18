package dto.response.bookresponse;

import hamtech.bookstorebookservice.entity.Book;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    String id;
    String title;
    String isbn;
    Double price;
    Double importPrice;
    Double discountPrice;
    Integer stockQuantity;
    LocalDate publishDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    String updatedBy;
    List<String> categoryId;
    String description;
    List<String> authorNames;
    List<String> categoryNames;
    List<String> imageUrls;
}