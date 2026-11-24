package com.example.board_game_buddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BoardGameBuddyApplication {

	public static void main(String[] args) {
		if (System.getenv("NARAYA_API_KEY") == null || System.getenv("NARAYA_API_KEY").isBlank()) {
			throw new IllegalStateException("NARAYA_API_KEY belum diset. Muat dari konfigurasi pi sebelum bootRun; lihat README.md.");
		}
		SpringApplication.run(BoardGameBuddyApplication.class, args);
	}

}
