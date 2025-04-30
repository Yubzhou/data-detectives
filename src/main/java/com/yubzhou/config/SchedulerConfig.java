package com.yubzhou.config;

import com.yubzhou.properties.SchedulerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.lang.reflect.Constructor;
import java.util.concurrent.RejectedExecutionHandler;

@Configuration
@EnableScheduling // 开启定时任务
@Slf4j
public class SchedulerConfig implements SchedulingConfigurer {

	private final SchedulerProperties schedulerProperties;

	@Autowired
	public SchedulerConfig(SchedulerProperties schedulerProperties) {
		this.schedulerProperties = schedulerProperties;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		// 设置自定义线程池
		taskRegistrar.setScheduler(schedulerTaskScheduler());
	}

	@Bean("schedulerTaskScheduler") // 指定 Bean 名称
	@Primary                        // 标记为首选的，如果不指定@Qualifier，则默认注入该Bean
	public ThreadPoolTaskScheduler schedulerTaskScheduler() {
		return buildScheduler(schedulerProperties.getScheduler());
	}

	// 构建线程池的通用方法
	private ThreadPoolTaskScheduler buildScheduler(SchedulerProperties.PoolConfig config) {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(config.getPoolSize()); // 线程池大小
		scheduler.setThreadNamePrefix(config.getThreadNamePrefix()); // 线程名前缀
		scheduler.setRejectedExecutionHandler(createRejectPolicy(config.getRejectPolicy())); // 拒绝策略
		scheduler.setWaitForTasksToCompleteOnShutdown(config.isWaitForTasksToCompleteOnShutdown()); // 关闭时等待任务完成
		scheduler.setAwaitTerminationSeconds(config.getAwaitTerminationSeconds()); // 设置等待超时时间，超时后强制关闭线程池
		scheduler.initialize(); // 初始化线程池
		return scheduler;
	}

	// 根据类名创建拒绝策略实例
	private RejectedExecutionHandler createRejectPolicy(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			RejectedExecutionHandler handler = (RejectedExecutionHandler) constructor.newInstance();
			log.info("创建拒绝策略实例: " + handler.getClass().getName());
			return handler;
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("拒绝策略类不存在: " + className, e);
		} catch (Exception e) {
			throw new RuntimeException("无法实例化拒绝策略: " + className, e);
		}
	}
}