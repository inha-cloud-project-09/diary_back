package com.diary.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DiaryBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiaryBackApplication.class, args);
    }

}