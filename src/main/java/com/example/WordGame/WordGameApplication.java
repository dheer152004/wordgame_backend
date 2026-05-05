package com.example.WordGame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WordGameApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordGameApplication.class, args);
		System.out.println("wordgame Backend Started on http://localhost:8080");
	}

}
