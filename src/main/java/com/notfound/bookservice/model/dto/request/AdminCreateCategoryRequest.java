package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateCategoryRequest {
    @NotBlank(message = "Tên thể loại không được để trống")
    @Size(max = 255)
    private String name;

    @Size(max = 10_000)
    private String description;
}
