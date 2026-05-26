package com.notfound.bookservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class AuthorResponse {
    private UUID id;
    private String name;
    private String biography;
    private LocalDate dateOfBirth;
    private String nationality;
    private Integer bookCount;
}
