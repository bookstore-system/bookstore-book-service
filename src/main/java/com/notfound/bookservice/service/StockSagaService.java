package com.notfound.bookservice.service;

import com.notfound.bookservice.messaging.dto.SagaMessageEnvelope;

public interface StockSagaService {

    void handleReserveCommand(SagaMessageEnvelope command);

    void handleConfirmCommand(SagaMessageEnvelope command);

    void handleReleaseCommand(SagaMessageEnvelope command);

    void expireStaleReservations();
}
