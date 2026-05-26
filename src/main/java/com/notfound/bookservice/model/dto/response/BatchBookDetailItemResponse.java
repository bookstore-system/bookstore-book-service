package com.notfound.bookservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BatchBookDetailItemResponse {
    private UUID bookId;
    private String title;
    private Double price;
    private Double salePrice;
    private Integer stockQuantity;
}
