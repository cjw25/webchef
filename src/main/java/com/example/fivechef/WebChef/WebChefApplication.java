package com.example.fivechef.WebChef;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class WebChefApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebChefApplication.class, args);
		System.out.println("-- Web Chef start --");
	}

}
