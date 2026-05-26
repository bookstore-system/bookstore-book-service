package com.notfound.bookservice.model.mapper;

import com.notfound.bookservice.model.dto.response.BookResponse;
import com.notfound.bookservice.model.dto.response.BookSummaryResponse;
import com.notfound.bookservice.model.entity.Author;
import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.BookImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "id", expression = "java(book.getId() != null ? book.getId().toString() : null)")
    @Mapping(target = "status", expression = "java(book.getStatus().name())")
    @Mapping(target = "authorNames", source = "authors", qualifiedByName = "authorNames")
    @Mapping(target = "categoryNames", source = "categories", qualifiedByName = "categoryNames")
    @Mapping(target = "categoryId", source = "categories", qualifiedByName = "categoryIdStrings")
    @Mapping(target = "imageUrls", source = "images", qualifiedByName = "imageUrls")
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "reviewCount", constant = "0")
    BookResponse toBookResponse(Book book);

    @Mapping(target = "mainImageUrl", source = "images", qualifiedByName = "mainImageUrl")
    @Mapping(target = "authorNames", source = "authors", qualifiedByName = "authorNames")
    @Mapping(target = "categoryId", source = "categories", qualifiedByName = "categoryIdStrings")
    @Mapping(target = "status", expression = "java(book.getStatus().name())")
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "reviewCount", constant = "0")
    BookSummaryResponse toBookSummaryResponse(Book book);

    @Named("authorNames")
    default List<String> authorNames(List<Author> authors) {
        if (authors == null) {
            return Collections.emptyList();
        }
        return authors.stream().map(Author::getName).toList();
    }

    @Named("categoryNames")
    default List<String> categoryNames(List<com.notfound.bookservice.model.entity.Category> categories) {
        if (categories == null) {
            return Collections.emptyList();
        }
        return categories.stream().map(com.notfound.bookservice.model.entity.Category::getName).toList();
    }

    @Named("categoryIdStrings")
    default List<String> categoryIdStrings(List<com.notfound.bookservice.model.entity.Category> categories) {
        if (categories == null) {
            return Collections.emptyList();
        }
        return categories.stream()
                .map(category -> category.getId().toString())
                .toList();
    }

    @Named("imageUrls")
    default List<String> imageUrls(List<BookImage> images) {
        if (images == null) {
            return Collections.emptyList();
        }
        return images.stream().map(BookImage::getUrl).toList();
    }

    @Named("mainImageUrl")
    default String mainImageUrl(List<BookImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0).getUrl();
    }
}
