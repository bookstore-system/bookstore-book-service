package com.notfound.bookservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "gemini-embedding-client",
        url = "${clients.gemini.url:https://generativelanguage.googleapis.com}"
)
public interface GeminiEmbeddingClient {

    @PostMapping("/v1beta/models/{model}:embedContent")
    Map<String, Object> embedContent(
            @PathVariable("model") String model,
            @RequestParam("key") String apiKey,
            @RequestBody Map<String, Object> body
    );
}
