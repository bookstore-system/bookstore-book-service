package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorFilterRequest {

    private String nationality;

    @Min(1000)
    @Max(2100)
    private Integer birthYear;

    @Pattern(regexp = "^(?i)(asc|desc)$", message = "sortByName chỉ được là asc hoặc desc")
    private String sortByName;

    @Pattern(regexp = "^(?i)(asc|desc)$", message = "sortByBirthYear chỉ được là asc hoặc desc")
    private String sortByBirthYear;

    @Min(0)
    @Builder.Default
    private int page = 0;

    @Min(1)
    @Max(100)
    @Builder.Default
    private int size = 10;
}
