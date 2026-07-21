package com.example.internal_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class InternalApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternalApiApplication.class, args);
	}

}
