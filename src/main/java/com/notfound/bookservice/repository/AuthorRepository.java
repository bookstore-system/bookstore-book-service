package com.notfound.bookservice.repository;

import com.notfound.bookservice.model.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthorRepository extends JpaRepository<Author, UUID> {

    boolean existsByNameIgnoreCase(String name);
}
