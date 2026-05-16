package com.notfound.bookservice.client;

import com.notfound.bookservice.client.config.QdrantFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "qdrant-client",
        url = "${clients.qdrant.url}",
        configuration = QdrantFeignConfig.class
)
public interface QdrantClient {

    @PutMapping("/collections/books/points")
    void upsertPoints(@RequestParam("wait") boolean wait, @RequestBody Map<String, Object> body);

    @PostMapping("/collections/books/points/search")
    Map<String, Object> searchPoints(@RequestBody Map<String, Object> body);

    @PostMapping("/collections/books/points/delete")
    void deletePoints(@RequestParam("wait") boolean wait, @RequestBody Map<String, Object> body);
}
