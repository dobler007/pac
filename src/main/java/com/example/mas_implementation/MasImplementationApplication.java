package com.example.mas_implementation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MasImplementationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MasImplementationApplication.class, args);
        System.out.println("Application running at http://localhost:8080/");
        System.out.println("H2 console available at http://localhost:8080/h2-console/");
        
    }
}
