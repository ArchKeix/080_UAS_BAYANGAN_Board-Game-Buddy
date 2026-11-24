package com.example.board_game_buddy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.ai.openai.api-key=test-key",
		"spring.ai.openai.base-url=http://localhost:1",
		"spring.ai.openai.chat.options.model=gpt-4o-mini"
})
class BoardGameBuddyApplicationTests {

	@Test
	void contextLoads() {
	}

}
