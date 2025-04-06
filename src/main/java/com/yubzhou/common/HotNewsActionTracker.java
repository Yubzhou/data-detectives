package com.yubzhou.common;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class HotNewsActionTracker {
	// 全局最后事件时间戳
	private final AtomicLong lastEventTime = new AtomicLong(0);

	// 标记新事件（Kafka消费者调用）
	public void markAction() {
		lastEventTime.set(System.currentTimeMillis());
	}

	/**
	 * 检查是否需要刷新（定时任务调用）
	 *
	 * @param timeWindowMillis 时间窗口（毫秒）
	 * @return 如果距离上次事件时间戳的间隔不超过时间窗口（代表有新数据，需要刷新缓存），则返回true，否则返回false
	 */
	public boolean shouldRefresh(boolean force, long timeWindowMillis) {
		long current = System.currentTimeMillis();
		return force || ((current - lastEventTime.get()) < timeWindowMillis);
	}
}