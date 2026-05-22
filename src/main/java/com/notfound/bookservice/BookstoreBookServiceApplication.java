package com.notfound.bookservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.notfound.bookservice.config.SagaStockProperties;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableConfigurationProperties(SagaStockProperties.class)
public class BookstoreBookServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreBookServiceApplication.class, args);
    }
}
