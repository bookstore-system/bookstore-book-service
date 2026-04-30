package com.notfound.bookservice.model.dto.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookSummaryResponse {
    private UUID id;
    private String title;
    private Double price;
    private Double discountPrice;
    private Integer stockQuantity;
    private String mainImageUrl;
    private List<String> authorNames;
}
