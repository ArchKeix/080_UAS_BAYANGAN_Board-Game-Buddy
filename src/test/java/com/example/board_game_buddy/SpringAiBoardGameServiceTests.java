package com.example.board_game_buddy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("live-ai")
class SpringAiBoardGameServiceTests {

	@Autowired
	private BoardGameService boardGameService;

	@Autowired
	private ChatClient.Builder chatClientBuilder;

	private RelevancyEvaluator relevancyEvaluator;
	private FactCheckingEvaluator factCheckingEvaluator;

	@BeforeEach
	void setup() {
		this.relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
		this.factCheckingEvaluator = new FactCheckingEvaluator(chatClientBuilder);
	}

	@Test
	void evaluateRelevancy() {
		String userText = "Why is the sky blue?";
		var answer = boardGameService.askQuestion(new Question(userText));
		var request = new EvaluationRequest(userText, answer.answer());
		var response = relevancyEvaluator.evaluate(request);
		assertThat(response.isPass())
				.withFailMessage("The answer \"%s\" is not considered relevant to the question \"%s\".",
						answer.answer(), userText)
				.isTrue();
	}

	@Test
	void evaluateFactualAccuracy() {
		String userText = "Why is the sky blue?";
		var answer = boardGameService.askQuestion(new Question(userText));
		var request = new EvaluationRequest(userText, answer.answer());
		var response = factCheckingEvaluator.evaluate(request);
		assertThat(response.isPass())
				.withFailMessage("The answer \"%s\" is not considered correct for the question \"%s\".",
						answer.answer(), userText)
				.isTrue();
	}
}
