package com.notfound.bookservice.messaging;

import com.notfound.bookservice.config.SagaStockProperties;
import com.notfound.bookservice.messaging.dto.SagaMessageEnvelope;
import com.notfound.bookservice.service.StockSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "bookstore.saga.enabled", havingValue = "true", matchIfMissing = true)
public class BookStockCommandConsumer {

    private final StockSagaService stockSagaService;

    @RabbitListener(queues = "${bookstore.saga.commands-queue:book.commands.queue}")
    public void onCommand(SagaMessageEnvelope command) {
        if (command == null || command.getType() == null) {
            log.warn("Received command without type");
            return;
        }
        log.info("Received stock command type={} sagaId={} eventId={}", command.getType(), command.getSagaId(), command.getEventId());
        switch (command.getType()) {
            case SagaMessageTypes.RESERVE_COMMAND -> stockSagaService.handleReserveCommand(command);
            case SagaMessageTypes.CONFIRM_COMMAND -> stockSagaService.handleConfirmCommand(command);
            case SagaMessageTypes.RELEASE_COMMAND -> stockSagaService.handleReleaseCommand(command);
            default -> log.warn("Unsupported command type={}", command.getType());
        }
    }
}
