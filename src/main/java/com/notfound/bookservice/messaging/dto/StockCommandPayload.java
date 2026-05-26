package com.notfound.bookservice.messaging.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockCommandPayload {
    private List<StockLineItem> items;
    private Integer ttlMinutes;
}
