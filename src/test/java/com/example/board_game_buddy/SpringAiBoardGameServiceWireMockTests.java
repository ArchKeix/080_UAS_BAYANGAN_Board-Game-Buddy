package com.example.board_game_buddy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "openai.base.url"))
@SpringBootTest(properties = {
		"spring.ai.openai.base-url=${openai.base.url}/v1",
		"spring.ai.openai.api-key=test-key",
		"spring.ai.openai.chat.options.model=gpt-4o-mini",
		"spring.ai.openai.chat.completions-path=/chat/completions"
})
@AutoConfigureMockMvc
class SpringAiBoardGameServiceWireMockTests {

	@Value("classpath:/test-openai-response.json")
	Resource responseResource;

	@Autowired
	ChatClient.Builder chatClientBuilder;

	@Autowired
	MockMvc mockMvc;

	@BeforeEach
	void setup() throws IOException {
		WireMock.reset();
		var cannedResponse = responseResource.getContentAsString(StandardCharsets.UTF_8);
		var responseNode = new ObjectMapper().readTree(cannedResponse);
		WireMock.stubFor(WireMock.post("/v1/chat/completions")
				.willReturn(ResponseDefinitionBuilder.okForJson(responseNode)));
	}

	@Test
	void testAskQuestion() {
		var service = new SpringAiBoardGameService(chatClientBuilder);
		var answer = service.askQuestion(new Question("What is the capital of France?"));
		assertThat(answer).isNotNull();
		assertThat(answer.answer()).isEqualTo("Paris");
	}

	@Test
	void postAskReturnsAnswerAsJson() throws Exception {
		mockMvc.perform(post("/ask")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"question\":\"What is the capital of France?\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answer").value("Paris"));
	}
}
