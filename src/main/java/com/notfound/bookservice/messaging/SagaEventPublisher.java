package com.notfound.bookservice.messaging;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.notfound.bookservice.config.SagaStockProperties;
import com.notfound.bookservice.messaging.dto.SagaMessageEnvelope;
import com.notfound.bookservice.messaging.dto.StockChangedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "bookstore.saga.enabled", havingValue = "true", matchIfMissing = true)
public class SagaEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final SagaStockProperties properties;
    private final ObjectMapper objectMapper;

    public void publishResult(SagaMessageEnvelope command, String eventType, String routingKey, Object payload) {
        SagaMessageEnvelope event = new SagaMessageEnvelope();
        event.setEventId(UUID.randomUUID());
        event.setSagaId(command.getSagaId());
        event.setCorrelationId(command.getCorrelationId() != null ? command.getCorrelationId() : command.getSagaId());
        event.setCausationId(command.getEventId());
        event.setType(eventType);
        event.setOccurredAt(Instant.now());
        event.setOrderId(command.getOrderId());
        event.setUserId(command.getUserId());
        event.setPayload(objectMapper.valueToTree(payload));

        rabbitTemplate.convertAndSend(properties.getEventsExchange(), routingKey, event);
        log.info("Published saga event type={} sagaId={} orderId={}", eventType, event.getSagaId(), event.getOrderId());
    }

    public void publishStockChanged(UUID sagaId, UUID orderId, StockChangedPayload changed) {
        SagaMessageEnvelope event = new SagaMessageEnvelope();
        event.setEventId(UUID.randomUUID());
        event.setSagaId(sagaId);
        event.setCorrelationId(sagaId);
        event.setType(SagaMessageTypes.CHANGED_EVENT);
        event.setOccurredAt(Instant.now());
        event.setOrderId(orderId);
        ObjectNode payload = objectMapper.valueToTree(changed);
        event.setPayload(payload);

        rabbitTemplate.convertAndSend(properties.getEventsExchange(), SagaMessageTypes.RK_CHANGED_EVENT, event);
        log.debug("Published stock.changed bookId={} sagaId={}", changed.getBookId(), sagaId);
    }
}
