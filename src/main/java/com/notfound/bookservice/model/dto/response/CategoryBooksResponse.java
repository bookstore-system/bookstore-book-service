package com.notfound.bookservice.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CategoryBooksResponse {
    private CategoryResponse category;
    private List<BookSummaryResponse> books;
}
