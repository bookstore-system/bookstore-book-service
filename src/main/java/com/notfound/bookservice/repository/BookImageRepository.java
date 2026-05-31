package com.notfound.bookservice.repository;

import com.notfound.bookservice.model.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookImageRepository extends JpaRepository<BookImage, UUID> {

    List<BookImage> findByBook_IdOrderByPriorityAscUploadedAtAsc(UUID bookId);

    Optional<BookImage> findByIdAndBook_Id(UUID id, UUID bookId);

    @Query("""
            SELECT image.book.id AS bookId,
                   image.url AS url
            FROM BookImage image
            WHERE image.book.id IN :bookIds
            ORDER BY image.book.id ASC, image.priority ASC, image.uploadedAt ASC
            """)
    List<BookThumbnailProjection> findThumbnailsByBookIdIn(@Param("bookIds") List<UUID> bookIds);

    interface BookThumbnailProjection {
        UUID getBookId();

        String getUrl();
    }
}
