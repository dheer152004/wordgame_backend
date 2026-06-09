package com.example.WordGame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WordGameApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(WordGameApplication.class, args);
		String port = context.getEnvironment().getProperty("server.port", "8080");
		System.out.println("wordgame Backend Started on http://localhost:" + port);
	}

}
