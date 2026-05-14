package com.notfound.bookservice.repository;

import com.notfound.bookservice.model.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query("SELECT c FROM Category c ORDER BY SIZE(c.books) DESC")
    List<Category> findPopularCategories(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
