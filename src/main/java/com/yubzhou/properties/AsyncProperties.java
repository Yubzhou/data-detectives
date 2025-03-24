package com.yubzhou.properties;

import com.yubzhou.common.SystemInfo;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "async")  // 绑定 application.properties 中 async 开头的配置
public class AsyncProperties {
	private PoolConfig sse;      // 对应 async.sse.*
	private PoolConfig upload;   // 对应 async.upload.*
	private PoolConfig global;  // 对应 async.global.*

	@Getter
	@Setter
	@ToString
	public static class PoolConfig {
		private int corePoolSize = SystemInfo.AVAILABLE_PROCESSORS; // 核心线程数
		private int maxPoolSize = SystemInfo.AVAILABLE_PROCESSORS * 2;  // 最大线程数=核心线程数+非核心线程数
		private int queueCapacity = 100; // 队列容量
		private int keepAliveSeconds = 60; // 非核心线程空闲存活时间
		private String threadNamePrefix = "async-"; // 线程名前缀
		// 拒绝策略的类全名（如 java.util.concurrent.ThreadPoolExecutor$AbortPolicy）
		// 默认为 AbortPolicy，即丢弃任务并抛出异常
		private String rejectPolicy = "java.util.concurrent.ThreadPoolExecutor$AbortPolicy";
		private boolean waitForTasksToCompleteOnShutdown = true; // 是否等待任务完成再关闭
		private int awaitTerminationSeconds = 60; // 等待任务完成的超时时间（秒），超时后强制关闭线程池
	}
}