package com.fishgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FishGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FishGoApplication.class, args);
	}

}
