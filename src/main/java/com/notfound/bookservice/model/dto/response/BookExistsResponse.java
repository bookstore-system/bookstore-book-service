package com.notfound.bookservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BookExistsResponse {
    private Boolean exists;
    private UUID bookId;
    private String title;
}
