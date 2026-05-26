package com.notfound.bookservice.service;

import com.notfound.bookservice.model.dto.request.AuthorFilterRequest;
import com.notfound.bookservice.model.dto.request.AuthorRequest;
import com.notfound.bookservice.model.dto.request.AuthorSearchRequest;
import com.notfound.bookservice.model.dto.response.AuthorResponse;
import com.notfound.bookservice.model.dto.response.PageResponse;

import java.util.UUID;

public interface AuthorService {

    PageResponse<AuthorResponse> getAuthors(int page, int size, String sortByName, String sortByBirthYear);

    PageResponse<AuthorResponse> searchByName(AuthorSearchRequest request);

    PageResponse<AuthorResponse> filterAuthors(AuthorFilterRequest request);

    AuthorResponse getAuthorById(UUID id);

    AuthorResponse createAuthor(AuthorRequest request);

    AuthorResponse updateAuthor(UUID id, AuthorRequest request);
}
