package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateBookRequest {
    @NotBlank
    private String title;
    private String isbn;
    @NotNull
    @Min(0)
    private Double price;
    @Min(0)
    private Double discountPrice;
    @Min(0)
    private Double importPrice;
    @NotNull
    @Min(0)
    private Integer stockQuantity;
    private LocalDate publishDate;
    private String description;
    private String status;
    private List<UUID> authorIds;
    private List<UUID> categoryIds;
    private List<String> imageUrls;
}
