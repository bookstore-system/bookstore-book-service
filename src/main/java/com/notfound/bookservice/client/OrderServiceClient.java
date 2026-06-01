package com.notfound.bookservice.client;

import com.notfound.bookservice.client.dto.ApiResponse;
import com.notfound.bookservice.client.dto.BookSalesStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "order-service", url = "${clients.order.url:http://order-service:8080}")
public interface OrderServiceClient {

    @GetMapping("/api/v1/orders/admin/book-sales-stats")
    ApiResponse<BookSalesStatsResponse> getBookSalesStats();
}
