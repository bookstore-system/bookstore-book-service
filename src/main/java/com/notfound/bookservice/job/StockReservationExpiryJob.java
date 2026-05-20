package com.notfound.bookservice.job;

import com.notfound.bookservice.service.StockSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "bookstore.saga.enabled", havingValue = "true", matchIfMissing = true)
public class StockReservationExpiryJob {

    private final StockSagaService stockSagaService;

    @Scheduled(fixedDelayString = "${bookstore.saga.expiry-job-delay-ms:300000}")
    public void expireReservations() {
        log.debug("Running stock reservation expiry job");
        stockSagaService.expireStaleReservations();
    }
}
