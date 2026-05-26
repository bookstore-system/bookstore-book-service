package com.notfound.bookservice.model.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookResponse {
    private String id;
    private String title;
    private String isbn;
    private Double price;
    private Double discountPrice;
    private Double importPrice;
    private Integer stockQuantity;
    private LocalDate publishDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<String> categoryId;
    private String description;
    private String status;
    private List<String> authorNames;
    private List<String> categoryNames;
    private List<String> imageUrls;
    private Double averageRating;
    private Integer reviewCount;
}
