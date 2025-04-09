package com.yubzhou.config;

import com.yubzhou.consumer.HotNewsCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class ShutdownEventListener {

	private final ThreadPoolTaskExecutor globalTaskExecutor;
	private final HotNewsCacheService hotNewsCacheService;
	private final RedisConnectionFactory redisConnectionFactory;

	@Autowired
	public ShutdownEventListener(@Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor,
								 HotNewsCacheService hotNewsCacheService,
								 RedisConnectionFactory redisConnectionFactory) {
		this.globalTaskExecutor = globalTaskExecutor;
		this.hotNewsCacheService = hotNewsCacheService;
		this.redisConnectionFactory = redisConnectionFactory;
	}

	@EventListener(ContextClosedEvent.class) // 监听容器关闭事件
	public void handleContextClosedEvent(ContextClosedEvent event) {
		// 提交任务到全局线程池
		CompletableFuture<Void> refreshFuture = refreshHotNewsCache();
		try {
			// 等待所有任务完成或超时
			log.info("系统正在停机，处理剩余任务...");
			refreshFuture.get();
			// 触发同步RDB持久化
			triggerRdbPersistence();
		} catch (InterruptedException e) {
			log.error("任务被中断", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			log.error("任务执行失败", e);
		} catch (Exception e) {
			log.error("发生未知异常", e);
		}
	}

	// 异步刷新缓存
	private CompletableFuture<Void> refreshHotNewsCache() {
		log.info("强制刷新热点新闻缓存...");
		return CompletableFuture.runAsync(() -> {
			// 刷新1h缓存
			hotNewsCacheService.refresh1hCache(true);
			// 刷新24h缓存
			hotNewsCacheService.refresh24hCache(true);
			// 刷新7d缓存
			hotNewsCacheService.refresh7dCache(true);
		}, globalTaskExecutor);
	}

	// 手动触发 Redis RDB 持久化
	private void triggerRdbPersistence() {
		log.info("开始执行Redis RDB持久化...");
		try (RedisConnection connection = redisConnectionFactory.getConnection()) {
			// 执行SAVE命令（同步阻塞）
			connection.serverCommands().save();
			log.info("Redis RDB持久化完成，数据已保存到磁盘");
		} catch (Exception e) {
			log.error("RDB持久化失败", e);
		}
	}
}