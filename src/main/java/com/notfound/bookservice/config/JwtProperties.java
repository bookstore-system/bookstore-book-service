package com.notfound.bookservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * Thư mục chứa public.pem (RS256) — cùng cấu hình user-service / api-gateway.
     */
    private String keysDir = "./key";
}
