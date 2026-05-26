package com.notfound.bookservice.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReduceStockRequest {
    @NotEmpty
    @Valid
    private List<ReduceStockItemRequest> items;
}
