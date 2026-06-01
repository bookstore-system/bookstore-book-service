package com.notfound.bookservice.client;

import com.notfound.bookservice.client.dto.ApiResponse;
import com.notfound.bookservice.client.dto.ReviewSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "review-service", url = "${clients.review.url:http://review-service:8080}")
public interface ReviewServiceClient {

    @GetMapping("/api/v1/reviews/book/{bookId}/summary")
    ApiResponse<ReviewSummaryResponse> getBookReviewSummary(@PathVariable UUID bookId);
}
