package com.notfound.bookservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookservice.config.SagaStockProperties;
import com.notfound.bookservice.messaging.SagaEventPublisher;
import com.notfound.bookservice.messaging.SagaMessageTypes;
import com.notfound.bookservice.messaging.dto.SagaMessageEnvelope;
import com.notfound.bookservice.messaging.dto.StockChangedPayload;
import com.notfound.bookservice.messaging.dto.StockCommandPayload;
import com.notfound.bookservice.messaging.dto.StockLineItem;
import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.ProcessedMessage;
import com.notfound.bookservice.model.entity.StockReservation;
import com.notfound.bookservice.model.entity.StockReservation.Status;
import com.notfound.bookservice.repository.BookRepository;
import com.notfound.bookservice.repository.ProcessedMessageRepository;
import com.notfound.bookservice.repository.StockReservationRepository;
import com.notfound.bookservice.service.StockSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "bookstore.saga.enabled", havingValue = "true", matchIfMissing = true)
public class StockSagaServiceImpl implements StockSagaService {

    private final BookRepository bookRepository;
    private final StockReservationRepository stockReservationRepository;
    private final ProcessedMessageRepository processedMessageRepository;
    private final SagaEventPublisher sagaEventPublisher;
    private final SagaStockProperties sagaStockProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void handleReserveCommand(SagaMessageEnvelope command) {
        if (!beginIdempotent(command)) {
            return;
        }
        try {
            StockCommandPayload payload = parsePayload(command);
            List<StockLineItem> items = requireItems(payload);
            if (allAlreadyReserved(command.getSagaId(), items)) {
                publishReserved(command, items);
                return;
            }
            int ttlMinutes = payload.getTtlMinutes() != null ? payload.getTtlMinutes() : sagaStockProperties.getReservationTtlMinutes();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(ttlMinutes);

            for (StockLineItem item : items) {
                UUID bookId = item.getBookId();
                int quantity = item.getQuantity();
                if (quantity <= 0) {
                    failReserve(command, "Invalid quantity for book " + bookId);
                    return;
                }
                if (!bookRepository.existsById(bookId)) {
                    failReserve(command, "Book not found: " + bookId);
                    return;
                }
                if (stockReservationRepository.findBySagaIdAndBookId(command.getSagaId(), bookId).isPresent()) {
                    continue;
                }
                int updated = bookRepository.reserveStockIfAvailable(bookId, quantity);
                if (updated == 0) {
                    failReserve(command, "Insufficient stock for book " + bookId);
                    return;
                }
                stockReservationRepository.save(StockReservation.builder()
                        .sagaId(command.getSagaId())
                        .orderId(command.getOrderId())
                        .bookId(bookId)
                        .quantity(quantity)
                        .status(Status.RESERVED)
                        .expiresAt(expiresAt)
                        .build());
            }

            publishReserved(command, items);
        } catch (Exception ex) {
            log.error("Reserve stock failed sagaId={}", command.getSagaId(), ex);
            failReserve(command, ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleConfirmCommand(SagaMessageEnvelope command) {
        if (!beginIdempotent(command)) {
            return;
        }
        List<StockReservation> reservations =
                stockReservationRepository.findBySagaIdAndStatus(command.getSagaId(), Status.RESERVED);
        if (reservations.isEmpty()) {
            List<StockReservation> confirmed =
                    stockReservationRepository.findBySagaIdAndStatus(command.getSagaId(), Status.CONFIRMED);
            if (!confirmed.isEmpty()) {
                publishConfirmed(command, confirmed);
                return;
            }
            failConfirm(command, "No reservations to confirm for saga " + command.getSagaId());
            return;
        }

        for (StockReservation reservation : reservations) {
            int updated = bookRepository.confirmReservedStock(reservation.getBookId(), reservation.getQuantity());
            if (updated == 0) {
                throw new IllegalStateException("Cannot confirm stock for book " + reservation.getBookId());
            }
            reservation.setStatus(Status.CONFIRMED);
            stockReservationRepository.save(reservation);
            publishStockChanged(command, reservation.getBookId());
        }
        publishConfirmed(command, reservations);
    }

    @Override
    @Transactional
    public void handleReleaseCommand(SagaMessageEnvelope command) {
        if (!beginIdempotent(command)) {
            return;
        }
        releaseReservations(command, Status.RESERVED);
    }

    @Override
    @Transactional
    public void expireStaleReservations() {
        List<StockReservation> expired = stockReservationRepository.findByStatusAndExpiresAtBefore(
                Status.RESERVED, LocalDateTime.now());
        Map<UUID, SagaMessageEnvelope> sagaCommands = new HashMap<>();
        for (StockReservation reservation : expired) {
            reservation.setStatus(Status.EXPIRED);
            stockReservationRepository.save(reservation);
            int updated = bookRepository.releaseReservedStock(reservation.getBookId(), reservation.getQuantity());
            if (updated > 0) {
                publishStockChangedForSaga(reservation.getSagaId(), reservation.getOrderId(), reservation.getBookId());
            }
            sagaCommands.computeIfAbsent(reservation.getSagaId(), sagaId -> {
                SagaMessageEnvelope synthetic = new SagaMessageEnvelope();
                synthetic.setSagaId(sagaId);
                synthetic.setOrderId(reservation.getOrderId());
                synthetic.setEventId(UUID.randomUUID());
                return synthetic;
            });
        }
        sagaCommands.values().forEach(cmd -> sagaEventPublisher.publishResult(
                cmd,
                SagaMessageTypes.RELEASED_EVENT,
                SagaMessageTypes.RK_RELEASED_EVENT,
                Map.of("reason", "EXPIRED")));
    }

    private void releaseReservations(SagaMessageEnvelope command, Status fromStatus) {
        List<StockReservation> reservations =
                stockReservationRepository.findBySagaIdAndStatus(command.getSagaId(), fromStatus);
        if (reservations.isEmpty()) {
            List<StockReservation> released =
                    stockReservationRepository.findBySagaIdAndStatus(command.getSagaId(), Status.RELEASED);
            if (!released.isEmpty()) {
                publishReleased(command);
            }
            return;
        }
        for (StockReservation reservation : reservations) {
            int updated = bookRepository.releaseReservedStock(reservation.getBookId(), reservation.getQuantity());
            if (updated == 0) {
                log.warn(
                        "Release skipped bookId={} sagaId={} — reserved_stock already adjusted",
                        reservation.getBookId(),
                        command.getSagaId());
            }
            reservation.setStatus(Status.RELEASED);
            stockReservationRepository.save(reservation);
            publishStockChanged(command, reservation.getBookId());
        }
        publishReleased(command);
    }

    private boolean allAlreadyReserved(UUID sagaId, List<StockLineItem> items) {
        for (StockLineItem item : items) {
            var existing = stockReservationRepository.findBySagaIdAndBookId(sagaId, item.getBookId());
            if (existing.isEmpty() || existing.get().getStatus() != Status.RESERVED) {
                return false;
            }
        }
        return true;
    }

    private boolean beginIdempotent(SagaMessageEnvelope command) {
        if (command.getEventId() == null || command.getSagaId() == null) {
            log.warn("Ignoring command without eventId or sagaId type={}", command.getType());
            return false;
        }
        if (processedMessageRepository.existsById(command.getEventId())) {
            log.info("Skipping duplicate command eventId={} type={}", command.getEventId(), command.getType());
            return false;
        }
        processedMessageRepository.save(ProcessedMessage.builder()
                .messageId(command.getEventId())
                .messageType(command.getType())
                .build());
        return true;
    }

    private StockCommandPayload parsePayload(SagaMessageEnvelope command) {
        if (command.getPayload() == null) {
            throw new IllegalArgumentException("Command payload is required");
        }
        return objectMapper.convertValue(command.getPayload(), StockCommandPayload.class);
    }

    private List<StockLineItem> requireItems(StockCommandPayload payload) {
        if (payload.getItems() == null || payload.getItems().isEmpty()) {
            throw new IllegalArgumentException("Command payload.items is required");
        }
        return payload.getItems();
    }

    private void publishReserved(SagaMessageEnvelope command, List<StockLineItem> items) {
        for (StockLineItem item : items) {
            publishStockChanged(command, item.getBookId());
        }
        sagaEventPublisher.publishResult(
                command, SagaMessageTypes.RESERVED_EVENT, SagaMessageTypes.RK_RESERVED_EVENT, Map.of());
    }

    private void publishConfirmed(SagaMessageEnvelope command, List<StockReservation> reservations) {
        for (StockReservation reservation : reservations) {
            publishStockChanged(command, reservation.getBookId());
        }
        sagaEventPublisher.publishResult(
                command, SagaMessageTypes.CONFIRMED_EVENT, SagaMessageTypes.RK_CONFIRMED_EVENT, Map.of());
    }

    private void publishReleased(SagaMessageEnvelope command) {
        sagaEventPublisher.publishResult(
                command, SagaMessageTypes.RELEASED_EVENT, SagaMessageTypes.RK_RELEASED_EVENT, Map.of());
    }

    private void failReserve(SagaMessageEnvelope command, String reason) {
        sagaEventPublisher.publishResult(
                command,
                SagaMessageTypes.FAILED_EVENT,
                SagaMessageTypes.RK_FAILED_EVENT,
                Map.of("reason", reason, "step", "reserve"));
        log.warn("Stock reserve failed sagaId={} reason={}", command.getSagaId(), reason);
    }

    private void failConfirm(SagaMessageEnvelope command, String reason) {
        sagaEventPublisher.publishResult(
                command,
                SagaMessageTypes.FAILED_EVENT,
                SagaMessageTypes.RK_FAILED_EVENT,
                Map.of("reason", reason, "step", "confirm"));
        log.warn("Stock confirm failed sagaId={} reason={}", command.getSagaId(), reason);
    }

    private void publishStockChanged(SagaMessageEnvelope command, UUID bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return;
        }
        StockChangedPayload payload = StockChangedPayload.builder()
                .bookId(bookId)
                .onHandStock(book.getOnHandStock())
                .reservedStock(book.getReservedStockSafe())
                .availableStock(book.getAvailableStock())
                .changedAt(Instant.now())
                .build();
        sagaEventPublisher.publishStockChanged(command.getSagaId(), command.getOrderId(), payload);
    }

    private void publishStockChangedForSaga(UUID sagaId, UUID orderId, UUID bookId) {
        SagaMessageEnvelope synthetic = new SagaMessageEnvelope();
        synthetic.setSagaId(sagaId);
        synthetic.setOrderId(orderId);
        publishStockChanged(synthetic, bookId);
    }
}
