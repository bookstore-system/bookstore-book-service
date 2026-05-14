package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Tên thể loại không được để trống")
    @Size(max = 255)
    private String name;

    @Size(max = 10_000)
    private String description;

    private UUID parentCategoryId;
}
