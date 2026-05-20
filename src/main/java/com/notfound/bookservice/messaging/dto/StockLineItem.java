package com.notfound.bookservice.messaging.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class StockLineItem {
    private UUID bookId;
    private Integer quantity;
}
