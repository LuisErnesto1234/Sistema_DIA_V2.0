package com.spring.udemy.controlagua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ControlAguaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ControlAguaApplication.class, args);
    }

}
