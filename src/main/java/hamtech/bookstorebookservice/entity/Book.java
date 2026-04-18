package hamtech.bookstorebookservice.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    String title;
    String isbn;
    Double price;
    Double discountPrice;
    Double importPrice;
    Integer stockQuantity;
    LocalDate publishDate;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    String updatedBy;

}
