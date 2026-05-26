package com.notfound.bookservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ValidateBookIdsResponse {
    private Boolean allValid;
    private List<UUID> invalidBookIds;
}
