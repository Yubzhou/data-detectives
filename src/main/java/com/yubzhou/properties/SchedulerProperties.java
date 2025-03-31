package com.yubzhou.properties;

import com.yubzhou.common.SystemInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "task")  // 绑定 application.properties 中 async 开头的配置
public class SchedulerProperties {
	private PoolConfig scheduler;      // 对应 task.scheduler.*

	@Getter
	@Setter
	@ToString
	public static class PoolConfig {
		private int poolSize = SystemInfo.AVAILABLE_PROCESSORS; // 线程池大小
		private String threadNamePrefix = "task-scheduler-"; // 线程名前缀
		// 拒绝策略的类全名（如 java.util.concurrent.ThreadPoolExecutor$AbortPolicy）
		// 默认为 AbortPolicy，即丢弃任务并抛出异常
		private String rejectPolicy = "java.util.concurrent.ThreadPoolExecutor$AbortPolicy";
		private boolean waitForTasksToCompleteOnShutdown = true; // 是否等待任务完成再关闭
		private int awaitTerminationSeconds = 60; // 等待任务完成的超时时间（秒），超时后强制关闭线程池
	}
}