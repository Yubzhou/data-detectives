package com.yubzhou.service.impl;

import com.yubzhou.service.SseAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class SseAsyncServiceImpl implements SseAsyncService {

	private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	// 订阅 SSE 事件
	public void subscribe(String requestId, SseEmitter emitter) {
		emitters.put(requestId, emitter);
		// 触发时机：调用 complete()
		emitter.onCompletion(() -> {
			cleanupEmitter(requestId);
			log.info("SseEmitter completed for requestId: {}", requestId);
		});
		// 触发时机：超时
		emitter.onTimeout(() -> {
			emitter.complete();
			cleanupEmitter(requestId);
			log.warn("SseEmitter timeout for requestId: {}", requestId);
		});
		// 触发时机：发生异常或调用 completeWithError()
		emitter.onError((e) -> {
			cleanupEmitter(requestId);
			log.error("SseEmitter error for requestId: {}", requestId, e);
		});
	}

	// 向指定客户端推送结果
	@Async("sseTaskExecutor")  // 使用配置的线程池执行此方法
	public void pushResult(String requestId, Object result) {
		SseEmitter emitter = emitters.get(requestId);
		if (emitter != null) {
			try {
				log.info("Sending result to client for requestId: {}", requestId);
				for (int i = 0; i < 5; i++) {
					emitter.send(SseEmitter.event().name("result").data(result));
					Thread.sleep(1000); // 延迟 1 秒
				}
				// 发送结束事件，并关闭 SSE 连接
				sendEndEvent(emitter);
			} catch (Exception e) {
				// 无论出现什么异常，都关闭 SSE 连接并移除订阅者
				emitter.completeWithError(e);
			}
		} else {
			log.warn("No SseEmitter found for requestId: {}", requestId);
		}
	}

	private void sendEndEvent(SseEmitter emitter) {
		try {
			emitter.send(SseEmitter.event().name("end").data("结束"));
			emitter.complete();
			log.info("End event sent to client");
		} catch (Exception e) {
			// 无论出现什么异常，都关闭 SSE 连接并移除订阅者
			emitter.completeWithError(e);
		}
	}

	// 清理资源
	private void cleanupEmitter(String requestId) {
		emitters.remove(requestId);
		log.info("SseEmitter清理完成: {}", requestId);
	}
}