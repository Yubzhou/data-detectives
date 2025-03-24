package com.yubzhou.consumer;

import com.yubzhou.service.SseAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResultConsumer {

	private final SseAsyncService sseAsyncServiceImpl;

	@Autowired
	public ResultConsumer(SseAsyncService sseAsyncServiceImpl) {
		this.sseAsyncServiceImpl = sseAsyncServiceImpl;
	}

	@KafkaListener(topics = "results", groupId = "sse-group")
	public void handleResult(@Header(KafkaHeaders.RECEIVED_KEY) String requestId,
							 @Payload String result) {
		log.info("Received result for requestId: {}, result: {}", requestId, result);
		sseAsyncServiceImpl.pushResult(requestId, result); // 通过SSE推送结果
	}
}