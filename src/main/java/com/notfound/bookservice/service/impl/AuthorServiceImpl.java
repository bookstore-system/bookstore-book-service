package com.notfound.bookservice.service.impl;

import com.notfound.bookservice.exception.ResourceNotFoundException;
import com.notfound.bookservice.model.dto.request.AuthorFilterRequest;
import com.notfound.bookservice.model.dto.request.AuthorRequest;
import com.notfound.bookservice.model.dto.request.AuthorSearchRequest;
import com.notfound.bookservice.model.dto.response.AuthorResponse;
import com.notfound.bookservice.model.dto.response.PageResponse;
import com.notfound.bookservice.model.entity.Author;
import com.notfound.bookservice.repository.AuthorRepository;
import com.notfound.bookservice.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuthorResponse> getAuthors(int page, int size, String sortByName, String sortByBirthYear) {
        AuthorFilterRequest request = AuthorFilterRequest.builder()
                .page(page)
                .size(size)
                .sortByName(sortByName)
                .sortByBirthYear(sortByBirthYear)
                .build();
        return filterAuthors(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuthorResponse> searchByName(AuthorSearchRequest request) {
        Pageable pageable = PageRequest.of(defaultPage(request.getPage()), defaultSize(request.getSize()));
        String keyword = StringUtils.hasText(request.getName()) ? request.getName().trim() : "";
        Page<Author> authorPage = authorRepository.searchByName(keyword, pageable);
        return buildPageResponse(authorPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuthorResponse> filterAuthors(AuthorFilterRequest request) {
        Pageable pageable = PageRequest.of(defaultPage(request.getPage()), defaultSize(request.getSize()), buildSort(request));
        String nationality = StringUtils.hasText(request.getNationality()) ? request.getNationality().trim() : null;
        Page<Author> authorPage =
                authorRepository.filterAuthors(nationality, request.getBirthYear(), pageable);
        return buildPageResponse(authorPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorResponse getAuthorById(UUID id) {
        Author author = authorRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        return mapToResponse(author, loadBookCountsPerAuthor());
    }

    @Override
    @Transactional
    public AuthorResponse createAuthor(AuthorRequest request) {
        String name = request.getName().trim();
        if (authorRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tên tác giả đã tồn tại: " + name);
        }
        Author saved = authorRepository.save(mapToEntity(request, Author.builder().build()));
        return mapToResponse(saved, loadBookCountsPerAuthor());
    }

    @Override
    @Transactional
    public AuthorResponse updateAuthor(UUID id, AuthorRequest request) {
        Author author = authorRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        String name = request.getName().trim();
        if (authorRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Tên tác giả đã tồn tại: " + name);
        }
        Author updated = authorRepository.save(mapToEntity(request, author));
        return mapToResponse(updated, loadBookCountsPerAuthor());
    }

    private PageResponse<AuthorResponse> buildPageResponse(Page<Author> authorPage) {
        Map<UUID, Long> bookCounts = loadBookCountsPerAuthor();
        return PageResponse.<AuthorResponse>builder()
                .content(authorPage.getContent().stream()
                        .map(author -> mapToResponse(author, bookCounts))
                        .toList())
                .currentPage(authorPage.getNumber())
                .totalPages(authorPage.getTotalPages())
                .totalElements(authorPage.getTotalElements())
                .build();
    }

    private Sort buildSort(AuthorFilterRequest request) {
        Sort sort = Sort.unsorted();
        if (StringUtils.hasText(request.getSortByName())) {
            sort = "asc".equalsIgnoreCase(request.getSortByName())
                    ? Sort.by("name").ascending()
                    : Sort.by("name").descending();
        }
        if (StringUtils.hasText(request.getSortByBirthYear())) {
            Sort birthYearSort = "asc".equalsIgnoreCase(request.getSortByBirthYear())
                    ? Sort.by("dateOfBirth").ascending()
                    : Sort.by("dateOfBirth").descending();
            sort = sort.isSorted() ? sort.and(birthYearSort) : birthYearSort;
        }
        return sort;
    }

    private Author mapToEntity(AuthorRequest request, Author author) {
        author.setName(request.getName().trim());
        author.setBiography(StringUtils.hasText(request.getBiography()) ? request.getBiography().trim() : null);
        author.setDateOfBirth(request.getDateOfBirth());
        author.setNationality(
                StringUtils.hasText(request.getNationality()) ? request.getNationality().trim() : null);
        return author;
    }

    private AuthorResponse mapToResponse(Author author, Map<UUID, Long> bookCounts) {
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .biography(author.getBiography())
                .dateOfBirth(author.getDateOfBirth())
                .nationality(author.getNationality())
                .bookCount(bookCounts.getOrDefault(author.getId(), 0L).intValue())
                .build();
    }

    private Map<UUID, Long> loadBookCountsPerAuthor() {
        return authorRepository.countBooksPerAuthor().stream()
                .collect(Collectors.toMap(
                        AuthorRepository.AuthorBookCountProjection::getAuthorId,
                        AuthorRepository.AuthorBookCountProjection::getBookCount));
    }

    private int defaultPage(int page) {
        return Math.max(page, 0);
    }

    private int defaultSize(int size) {
        return size <= 0 ? 10 : Math.min(size, 100);
    }
}
