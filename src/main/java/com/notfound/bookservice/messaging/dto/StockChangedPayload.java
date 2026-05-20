package com.notfound.bookservice.messaging.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class StockChangedPayload {
    private UUID bookId;
    private int onHandStock;
    private int reservedStock;
    private int availableStock;
    private Instant changedAt;
}
