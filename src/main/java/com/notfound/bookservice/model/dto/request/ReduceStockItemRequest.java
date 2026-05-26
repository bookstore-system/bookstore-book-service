package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReduceStockItemRequest {
    @NotNull
    private UUID bookId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
