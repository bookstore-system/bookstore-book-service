package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookFilterRequest {
    private String keyword;

    @DecimalMin(value = "0.0")
    private Double minPrice;

    @DecimalMin(value = "0.0")
    private Double maxPrice;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Double minRating;

    private LocalDate publishedAfter;
    private UUID categoryId;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 10;

    @AssertTrue(message = "maxPrice must be greater than or equal to minPrice")
    public boolean isValidPriceRange() {
        if (minPrice != null && maxPrice != null) {
            return maxPrice >= minPrice;
        }
        return true;
    }
}
