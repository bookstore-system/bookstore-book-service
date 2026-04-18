package dto.request.bookrequest.bookrequest;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBookRequest {
    String title;
    String isbn;
    Double price;
    Double discountPrice;
    Double importPrice;
    Integer stockQuantity;
    LocalDate publishDate;
    String description;
    String status;
}