package com.notfound.bookservice.repository;

import com.notfound.bookservice.model.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AuthorRepository extends JpaRepository<Author, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query("SELECT a FROM Author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Author> searchByName(@Param("name") String name, Pageable pageable);

    @Query("SELECT a FROM Author a WHERE "
            + "(:nationality IS NULL OR a.nationality = :nationality) AND "
            + "(:birthYear IS NULL OR YEAR(a.dateOfBirth) = :birthYear)")
    Page<Author> filterAuthors(
            @Param("nationality") String nationality,
            @Param("birthYear") Integer birthYear,
            Pageable pageable);

    @Query("SELECT a.id AS authorId, COUNT(b) AS bookCount FROM Author a LEFT JOIN a.books b GROUP BY a.id")
    List<AuthorBookCountProjection> countBooksPerAuthor();

    interface AuthorBookCountProjection {
        UUID getAuthorId();

        long getBookCount();
    }
}
