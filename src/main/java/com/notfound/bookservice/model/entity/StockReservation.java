package com.notfound.bookservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "stock_reservation",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_reservation_saga_book", columnNames = {"saga_id", "book_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockReservation {

    @Id
    @GeneratedValue
    @UuidGenerator
    UUID reservationId;

    @Column(name = "saga_id", nullable = false)
    UUID sagaId;

    @Column(name = "order_id", nullable = false)
    UUID orderId;

    @Column(name = "book_id", nullable = false)
    UUID bookId;

    @Column(nullable = false)
    Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public enum Status {
        RESERVED,
        CONFIRMED,
        RELEASED,
        EXPIRED
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
