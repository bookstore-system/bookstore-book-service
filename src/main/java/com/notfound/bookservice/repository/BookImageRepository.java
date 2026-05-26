package com.notfound.bookservice.repository;

import com.notfound.bookservice.model.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookImageRepository extends JpaRepository<BookImage, UUID> {

    List<BookImage> findByBook_IdOrderByPriorityAscUploadedAtAsc(UUID bookId);

    Optional<BookImage> findByIdAndBook_Id(UUID id, UUID bookId);
}
