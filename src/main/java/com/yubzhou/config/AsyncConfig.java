package com.yubzhou.config;

import com.yubzhou.properties.AsyncProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Constructor;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // 启用异步支持
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

	private final AsyncProperties asyncProperties;

	// 通过构造器注入配置
	@Autowired
	public AsyncConfig(AsyncProperties asyncProperties) {
		this.asyncProperties = asyncProperties;
	}

	// SSE 线程池
	@Bean("sseTaskExecutor") // 指定 Bean 名称
	public ThreadPoolTaskExecutor sseTaskExecutor() {
		return buildExecutor(asyncProperties.getSse());
	}

	// 文件上传 线程池
	@Bean("uploadTaskExecutor") // 指定 Bean 名称
	public ThreadPoolTaskExecutor uploadTaskExecutor() {
		return buildExecutor(asyncProperties.getUpload());
	}

	// 显式定义全局线程池 Bean
	@Bean("globalTaskExecutor") // 指定 Bean 名称
	@Primary                    // 标记为首选的，如果不指定@Qualifier，则默认注入该Bean
	public ThreadPoolTaskExecutor globalTaskExecutor() {
		return buildExecutor(asyncProperties.getGlobal());
	}

	// 全局默认线程池：覆盖 AsyncConfigurer 接口方法，返回全局线程池
	@Override
	public Executor getAsyncExecutor() {
		return globalTaskExecutor();
	}

	// 构建线程池的通用方法
	private ThreadPoolTaskExecutor buildExecutor(AsyncProperties.PoolConfig config) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(config.getCorePoolSize()); // 核心线程数
		executor.setMaxPoolSize(config.getMaxPoolSize()); // 最大线程数=核心线程数+非核心线程数
		executor.setQueueCapacity(config.getQueueCapacity()); // 队列容量
		executor.setKeepAliveSeconds(config.getKeepAliveSeconds()); // 非核心线程空闲存活时间
		executor.setThreadNamePrefix(config.getThreadNamePrefix()); // 线程名前缀
		// executor.setThreadGroupName("AsyncGroup");
		executor.setRejectedExecutionHandler(createRejectPolicy(config.getRejectPolicy())); // 拒绝策略
		executor.setWaitForTasksToCompleteOnShutdown(config.isWaitForTasksToCompleteOnShutdown()); // 关闭时等待任务完成
		executor.setAwaitTerminationSeconds(config.getAwaitTerminationSeconds()); // 设置等待超时时间，超时后强制关闭线程池
		executor.initialize(); // 初始化线程池
		return executor;
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

	// 异步异常处理器
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (ex, method, params) -> {
			log.error("异步任务执行异常: {}.{}", method.getDeclaringClass().getSimpleName(), method.getName(), ex);
		};
	}

	// 文件上传使用自定义拒绝策略：记录日志后丢弃
	public static class FileUploadRejectedExecutionHandler implements RejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			log.warn("File upload queue full, rejecting task: {}", r.toString());
		}
	}
}