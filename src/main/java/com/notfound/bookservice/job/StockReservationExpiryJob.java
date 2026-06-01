package com.notfound.bookservice.job;

import com.notfound.bookservice.service.StockSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "bookstore.saga.enabled", havingValue = "true", matchIfMissing = true)
public class StockReservationExpiryJob {

    private static final String LOCK_NAME = "bookstore-book-service:stock-reservation-expiry";

    private final StockSagaService stockSagaService;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(fixedDelayString = "${bookstore.saga.expiry-job-delay-ms:300000}")
    public void expireReservations() {
        if (!tryAcquireLock()) {
            log.debug("Skipping stock reservation expiry job because another pod holds the lock");
            return;
        }

        log.debug("Running stock reservation expiry job");
        try {
            stockSagaService.expireStaleReservations();
        } finally {
            releaseLock();
        }
    }

    private boolean tryAcquireLock() {
        try {
            Integer locked = jdbcTemplate.queryForObject("SELECT GET_LOCK(?, 0)", Integer.class, LOCK_NAME);
            return locked != null && locked == 1;
        } catch (DataAccessException ex) {
            log.warn("Unable to acquire stock reservation expiry lock; skipping this run: {}", ex.getMessage());
            return false;
        }
    }

    private void releaseLock() {
        try {
            jdbcTemplate.queryForObject("SELECT RELEASE_LOCK(?)", Integer.class, LOCK_NAME);
        } catch (DataAccessException ex) {
            log.warn("Unable to release stock reservation expiry lock: {}", ex.getMessage());
        }
    }
}
