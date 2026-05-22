package com.notfound.bookservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "bookstore.saga")
public class SagaStockProperties {
    private boolean enabled = true;
    private String commandsExchange = "bookstore.commands";
    private String eventsExchange = "bookstore.events";
    private String commandsQueue = "book.commands.queue";
    private int reservationTtlMinutes = 30;
}
