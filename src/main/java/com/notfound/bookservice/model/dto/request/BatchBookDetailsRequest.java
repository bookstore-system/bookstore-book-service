package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BatchBookDetailsRequest {
    @NotEmpty
    private List<UUID> bookIds;
}
