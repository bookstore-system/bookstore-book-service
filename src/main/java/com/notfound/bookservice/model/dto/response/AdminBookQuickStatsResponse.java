package com.notfound.bookservice.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookQuickStatsResponse {
    private Long activeBookCount;
    private Long lowStockBookCount;
    private Integer lowStockThreshold;
    private BigDecimal totalBookRevenue;
    private Long soldBookCount;
    private BigDecimal averageRevenuePerBook;
    private String currency;
}
