package com.notfound.bookservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BatchBookItemResponse {
    private UUID id;
    private String title;
    private Double price;
    private Double discountPrice;
    private String thumbnailUrl;
    private Boolean inStock;
}
