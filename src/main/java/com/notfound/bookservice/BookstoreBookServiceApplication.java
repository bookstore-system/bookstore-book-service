package com.notfound.bookservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BookstoreBookServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreBookServiceApplication.class, args);
    }
}
