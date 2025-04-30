package com.yubzhou.controller;

import com.yubzhou.service.SseAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/sse")
@Slf4j
// @JwtIgnore
public class SseController {

	private final SseAsyncService sseAsyncServiceImpl;

	@Autowired
	public SseController(SseAsyncService sseAsyncServiceImpl) {
		this.sseAsyncServiceImpl = sseAsyncServiceImpl;
	}

	// 客户端订阅SSE
	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe() {
		String requestId = UUID.randomUUID().toString();
		SseEmitter emitter = new SseEmitter(30_000L);
		try {
			emitter.send(SseEmitter.event().data(requestId).name("requestId")); // 立即推送 requestId
		} catch (IOException e) {
			emitter.completeWithError(e);
		}
		sseAsyncServiceImpl.subscribe(requestId, emitter);
		log.info("Client subscribed, requestId: {}", requestId);
		return emitter;
	}
}