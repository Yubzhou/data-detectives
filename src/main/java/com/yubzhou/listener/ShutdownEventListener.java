package com.yubzhou.listener;

import com.yubzhou.properties.AsyncProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class ShutdownEventListener {

	private final AsyncProperties asyncProperties;
	private final ThreadPoolTaskExecutor globalTaskExecutor;

	@Autowired
	public ShutdownEventListener(AsyncProperties asyncProperties,
								 @Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor) {
		this.asyncProperties = asyncProperties;
		this.globalTaskExecutor = globalTaskExecutor;
	}

	@EventListener
	public void handleContextClosedEvent(ContextClosedEvent event) {
		// 提交任务到全局线程池
		Future<?> future = globalTaskExecutor.submit(this::syncData);

		try {
			future.get(asyncProperties.getGlobal().getAwaitTerminationSeconds(), TimeUnit.SECONDS); // 等待任务完成（带超时）
			log.info("数据同步成功");
		} catch (TimeoutException e) {
			future.cancel(true);
			log.error("数据同步超时，任务已取消");
		} catch (Exception e) {
			log.error("数据同步失败", e);
		}
	}

	private void syncData() {
		// 模拟数据同步操作（耗时 5 秒）
		try {
			System.out.println("开始同步数据...");
			Thread.sleep(5000);
			System.out.println("数据同步成功");
		} catch (InterruptedException e) {
			System.err.println("数据同步被中断");
			Thread.currentThread().interrupt();
		}
	}
}