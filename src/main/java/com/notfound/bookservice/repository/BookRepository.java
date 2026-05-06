package com.notfound.bookservice.repository;

import com.notfound.bookservice.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.authors a " +
            "LEFT JOIN b.categories c " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(COALESCE(a.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b WHERE b.isbn = :isbn AND b.id <> :id")
    boolean existsByIsbnAndIdNot(@Param("isbn") String isbn, @Param("id") UUID id);

    @Query("""
            SELECT DISTINCT b FROM Book b
            LEFT JOIN b.categories c
            WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice IS NULL OR b.discountPrice >= :minPrice OR (b.discountPrice IS NULL AND b.price >= :minPrice))
              AND (:maxPrice IS NULL OR b.discountPrice <= :maxPrice OR (b.discountPrice IS NULL AND b.price <= :maxPrice))
              AND (:publishedAfter IS NULL OR b.publishDate >= :publishedAfter)
              AND (:categoryId IS NULL OR c.id = :categoryId)
            """)
    Page<Book> findByFilters(
            @Param("keyword") String keyword,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("publishedAfter") LocalDate publishedAfter,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT b FROM Book b JOIN b.categories c WHERE c.id = :categoryId")
    Page<Book> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("SELECT b FROM Book b ORDER BY b.stockQuantity ASC, b.createdAt DESC")
    List<Book> findBestSellingBooks(Pageable pageable);

    @Query("SELECT b FROM Book b ORDER BY function('RAND')")
    List<Book> findRandomBooks(Pageable pageable);

    @Modifying
    @Query("UPDATE Book b SET b.stockQuantity = b.stockQuantity - :quantity WHERE b.id = :bookId AND b.stockQuantity >= :quantity")
    int decreaseStockIfEnough(@Param("bookId") UUID bookId, @Param("quantity") Integer quantity);
}
