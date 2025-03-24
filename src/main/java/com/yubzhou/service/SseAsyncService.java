package com.yubzhou.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseAsyncService {

	// 订阅 SSE 事件
	void subscribe(String requestId, SseEmitter emitter);

	// 向指定客户端推送结果
	void pushResult(String requestId, Object result);
}
