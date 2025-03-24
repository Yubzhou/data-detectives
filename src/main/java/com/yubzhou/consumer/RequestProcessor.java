package com.yubzhou.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestProcessor {
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@KafkaListener(topics = "requests", groupId = "request-processor")
	public void handleRequest(@Header(KafkaHeaders.RECEIVED_KEY) String requestId,
							  @Payload String data) {
		String result = "processed: " + data; // 模拟处理逻辑
		log.info("send result to kafka topic: results, requestId: {}, result: {}", requestId, result);
		kafkaTemplate.send("results", requestId, result); // 发送结果
	}
}