package com.notfound.bookservice.repository;

import com.notfound.bookservice.model.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query("SELECT c FROM Category c ORDER BY SIZE(c.books) DESC")
    List<Category> findPopularCategories(Pageable pageable);

    @Query("SELECT DISTINCT c FROM Category c "
            + "LEFT JOIN FETCH c.parentCategory "
            + "LEFT JOIN FETCH c.subCategories "
            + "WHERE c.id = :id")
    Optional<Category> findDetailedById(@Param("id") UUID id);

    @Query("SELECT DISTINCT c FROM Category c "
            + "LEFT JOIN FETCH c.parentCategory "
            + "LEFT JOIN FETCH c.subCategories")
    List<Category> findAllWithHierarchy();

    @Query("SELECT c.id AS categoryId, COUNT(b) AS bookCount FROM Category c LEFT JOIN c.books b GROUP BY c.id")
    List<CategoryBookCountProjection> countBooksPerCategory();

    interface CategoryBookCountProjection {
        UUID getCategoryId();

        long getBookCount();
    }

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b JOIN b.categories c WHERE c.id = :categoryId")
    boolean hasBooks(@Param("categoryId") UUID categoryId);
}
