package com.notfound.bookservice.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFullDetailResponse {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<AuthorInfo> authors;
    private List<CategoryInfo> categories;
    private List<ImageInfo> images;
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private UUID id;
        private String name;
        private String biography;
        private LocalDate dateOfBirth;
        private String nationality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private UUID id;
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private UUID id;
        private String url;
        private Integer priority;
        private LocalDateTime uploadedAt;
    }
}
