package com.yubzhou.config;

import com.yubzhou.consumer.HotNewsCacheService;
import com.yubzhou.properties.AsyncProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
@Slf4j
public class ShutdownEventListener {

	private final AsyncProperties asyncProperties;
	private final ThreadPoolTaskExecutor globalTaskExecutor;
	private final HotNewsCacheService hotNewsCacheService;

	@Autowired
	public ShutdownEventListener(AsyncProperties asyncProperties,
								 @Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor, HotNewsCacheService hotNewsCacheService) {
		this.asyncProperties = asyncProperties;
		this.globalTaskExecutor = globalTaskExecutor;
		this.hotNewsCacheService = hotNewsCacheService;
	}

	@EventListener(ContextClosedEvent.class) // 监听容器关闭事件
	public void handleContextClosedEvent(ContextClosedEvent event) {
		// 提交任务到全局线程池
		CompletableFuture<Void> refreshFuture = refreshHotNewsCache();
		try {
			// 等待所有任务完成或超时
			log.info("系统正在停机...");
			CompletableFuture.allOf(refreshFuture).get(asyncProperties.getGlobal().getAwaitTerminationSeconds(), TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException e) {
			log.error("任务执行失败", e);
		} catch (TimeoutException e) {
			log.error("任务执行超时", e);
		} catch (Exception e) {
			log.error("任务执行异常", e);
		}
	}

	// 异步刷新缓存
	private CompletableFuture<Void> refreshHotNewsCache() {
		log.info("正在强制刷新热点新闻缓存...");
		return CompletableFuture.runAsync(() -> {
			// 刷新1h缓存
			hotNewsCacheService.refresh1hCache(true);
			// 刷新24h缓存
			hotNewsCacheService.refresh24hCache(true);
			// 刷新7d缓存
			hotNewsCacheService.refresh7dCache(true);
		}, globalTaskExecutor);
	}
}