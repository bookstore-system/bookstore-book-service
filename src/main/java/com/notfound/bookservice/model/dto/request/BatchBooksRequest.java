package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BatchBooksRequest {
    @NotEmpty
    private List<UUID> ids;
}
