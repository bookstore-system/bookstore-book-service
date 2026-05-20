package com.notfound.bookservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.notfound.bookservice.config.SagaStockProperties;
import com.notfound.bookservice.messaging.SagaEventPublisher;
import com.notfound.bookservice.messaging.dto.SagaMessageEnvelope;
import com.notfound.bookservice.messaging.dto.StockCommandPayload;
import com.notfound.bookservice.messaging.dto.StockLineItem;
import com.notfound.bookservice.model.entity.Book;
import com.notfound.bookservice.model.entity.StockReservation;
import com.notfound.bookservice.model.entity.StockReservation.Status;
import com.notfound.bookservice.repository.BookRepository;
import com.notfound.bookservice.repository.ProcessedMessageRepository;
import com.notfound.bookservice.repository.StockReservationRepository;
import com.notfound.bookservice.service.impl.StockSagaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockSagaServiceImplTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    StockReservationRepository stockReservationRepository;

    @Mock
    ProcessedMessageRepository processedMessageRepository;

    @Mock
    SagaEventPublisher sagaEventPublisher;

    @Mock
    SagaStockProperties sagaStockProperties;

    @InjectMocks
    StockSagaServiceImpl stockSagaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID sagaId;
    private UUID orderId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        stockSagaService =
                new StockSagaServiceImpl(
                        bookRepository,
                        stockReservationRepository,
                        processedMessageRepository,
                        sagaEventPublisher,
                        sagaStockProperties,
                        objectMapper);
        sagaId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        when(processedMessageRepository.existsById(any())).thenReturn(false);
    }

    @Test
    void reserve_increasesReservedStockAndPublishesReserved() {
        when(sagaStockProperties.getReservationTtlMinutes()).thenReturn(30);
        SagaMessageEnvelope command = reserveCommand(2);
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(stockReservationRepository.findBySagaIdAndBookId(sagaId, bookId)).thenReturn(Optional.empty());
        when(bookRepository.reserveStockIfAvailable(bookId, 2)).thenReturn(1);
        when(stockReservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Book book = Book.builder().id(bookId).stockQuantity(10).reservedStock(2).build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        stockSagaService.handleReserveCommand(command);

        ArgumentCaptor<StockReservation> captor = ArgumentCaptor.forClass(StockReservation.class);
        verify(stockReservationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.RESERVED);
        assertThat(captor.getValue().getQuantity()).isEqualTo(2);
        verify(sagaEventPublisher).publishResult(any(), eq("book.stock.reserved"), eq("book.stock.reserved"), any());
    }

    @Test
    void reserve_publishesFailedWhenInsufficientStock() {
        when(sagaStockProperties.getReservationTtlMinutes()).thenReturn(30);
        SagaMessageEnvelope command = reserveCommand(5);
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(stockReservationRepository.findBySagaIdAndBookId(sagaId, bookId)).thenReturn(Optional.empty());
        when(bookRepository.reserveStockIfAvailable(bookId, 5)).thenReturn(0);

        stockSagaService.handleReserveCommand(command);

        verify(stockReservationRepository, never()).save(any());
        verify(sagaEventPublisher).publishResult(any(), eq("book.stock.failed"), eq("book.stock.failed"), any());
    }

    @Test
    void confirm_decreasesOnHandAndReserved() {
        SagaMessageEnvelope command = simpleCommand("book.stock.confirm.command");
        StockReservation reservation = StockReservation.builder()
                .sagaId(sagaId)
                .orderId(orderId)
                .bookId(bookId)
                .quantity(2)
                .status(Status.RESERVED)
                .build();
        when(stockReservationRepository.findBySagaIdAndStatus(sagaId, Status.RESERVED))
                .thenReturn(List.of(reservation));
        when(bookRepository.confirmReservedStock(bookId, 2)).thenReturn(1);
        Book book = Book.builder().id(bookId).stockQuantity(8).reservedStock(0).build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        stockSagaService.handleConfirmCommand(command);

        assertThat(reservation.getStatus()).isEqualTo(Status.CONFIRMED);
        verify(sagaEventPublisher).publishResult(any(), eq("book.stock.confirmed"), eq("book.stock.confirmed"), any());
    }

    private SagaMessageEnvelope reserveCommand(int quantity) {
        SagaMessageEnvelope command = simpleCommand("book.stock.reserve.command");
        StockCommandPayload payload = new StockCommandPayload();
        StockLineItem item = new StockLineItem();
        item.setBookId(bookId);
        item.setQuantity(quantity);
        payload.setItems(List.of(item));
        command.setPayload(objectMapper.valueToTree(payload));
        return command;
    }

    private SagaMessageEnvelope simpleCommand(String type) {
        SagaMessageEnvelope command = new SagaMessageEnvelope();
        command.setEventId(UUID.randomUUID());
        command.setSagaId(sagaId);
        command.setOrderId(orderId);
        command.setType(type);
        command.setPayload(JsonNodeFactory.instance.objectNode());
        return command;
    }
}
