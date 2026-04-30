package com.notfound.bookservice.model.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class BookResponse {
    private UUID id;
    private String title;
    private String isbn;
    private Double price;
    private Double discountPrice;
    private Double importPrice;
    private Integer stockQuantity;
    private LocalDate publishDate;
    private String description;
    private String status;
    private List<String> authorNames;
    private List<String> categoryNames;
    private List<String> imageUrls;
}
