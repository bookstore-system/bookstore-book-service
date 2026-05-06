package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BookSortRequest {
    @Pattern(regexp = "price_asc|price_desc|title_asc|title_desc|date_asc|date_desc")
    private String sortType = "date_desc";

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 10;
}
