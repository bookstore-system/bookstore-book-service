package com.notfound.bookservice.repository;

import com.notfound.bookservice.model.entity.StockReservation;
import com.notfound.bookservice.model.entity.StockReservation.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    Optional<StockReservation> findBySagaIdAndBookId(UUID sagaId, UUID bookId);

    List<StockReservation> findBySagaIdAndStatus(UUID sagaId, Status status);

    List<StockReservation> findByStatusAndExpiresAtBefore(Status status, LocalDateTime expiresAt);
}
