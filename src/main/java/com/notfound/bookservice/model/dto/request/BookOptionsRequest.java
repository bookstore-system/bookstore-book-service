package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookOptionsRequest {
    @DecimalMin(value = "0.0")
    private Double minPrice;

    @DecimalMin(value = "0.0")
    private Double maxPrice;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Double minRating;

    private String option = "phobien";
    private List<UUID> categoryIds;

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
