package com.yubzhou.controller;

import com.yubzhou.common.KafkaConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	public ApiController(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@PostMapping("/process")
	public ResponseEntity<String> processRequest(
			@RequestBody Map<String, String> requestData // 包含前端传来的 requestId 和 data
	) {
		String requestId = requestData.get("requestId");
		String data = requestData.get("data");

		assertHasText(requestId, data);

		log.info("send request to kafka topic: requests, requestId: {}, data: {}", requestId, data);
		kafkaTemplate.send(KafkaConstant.REQUEST_TOPIC, requestId, data);
		return ResponseEntity.ok("Request accepted");
	}

	private void assertHasText(String... args) {
		for (String arg : args) {
			if (arg == null || arg.trim().isEmpty()) {
				throw new IllegalArgumentException("Argument cannot be null or empty");
			}
		}
	}
}