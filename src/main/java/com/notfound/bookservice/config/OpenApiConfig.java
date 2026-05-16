package com.notfound.bookservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookstoreBookServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bookstore Book Service")
                        .description("API quản lý sách, tác giả, danh mục và tồn kho (microservice).")
                        .version("1.0"));
    }
}
