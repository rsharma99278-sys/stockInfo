package com.stockinfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the StockInfo backend application.
 *
 * StockInfo - Stock Market Information and Portfolio Management System
 * BCA Final Year Major Project
 */
@SpringBootApplication
@EnableScheduling
public class StockInfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockInfoApplication.class, args);
    }

}
