package com.vakilsahay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * VakilSahay — Plain-Language Legal Document Explainer
 * © 2025 VakilSahay. All rights reserved.
 *
 * Proprietary clause-severity scoring algorithm and Indian legal
 * clause taxonomy are protected under Indian Copyright Act 1957.
 */
@SpringBootApplication
@EnableAsync
public class VakilSahayApplication {
    public static void main(String[] args) {
        SpringApplication.run(VakilSahayApplication.class, args);
    }
}