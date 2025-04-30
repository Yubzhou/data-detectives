package com.yubzhou;

import com.yubzhou.common.SystemInfo;
import com.yubzhou.properties.AsyncProperties;
import com.yubzhou.properties.CorsProperties;
import com.yubzhou.properties.SchedulerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan // 自动扫描并注册所有 @ConfigurationProperties 类
@Slf4j
public class SpringbootApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SpringbootApplication.class, args);

		AsyncProperties asyncProperties = context.getBean(AsyncProperties.class);
		SchedulerProperties schedulerProperties = context.getBean(SchedulerProperties.class);
		CorsProperties corsProperties = context.getBean(CorsProperties.class);
		printConfig(asyncProperties, schedulerProperties, corsProperties);

		log.info("系统初始化完成，服务已启动...");
	}

	public static void printConfig(AsyncProperties asyncProperties,
								   SchedulerProperties schedulerProperties,
								   CorsProperties corsProperties) {
		// 输出系统可用核心数
		log.info("systemInfo.availableProcessors: {}", SystemInfo.AVAILABLE_PROCESSORS);

		// 输出线程池配置信息
		log.info("sse线程池配置信息：{}", asyncProperties.getSse());
		log.info("上传线程池配置信息：{}", asyncProperties.getUpload());
		log.info("全局线程池配置信息：{}", asyncProperties.getGlobal());

		// 输出定时任务配置信息
		log.info("定时任务线程池配置信息：{}", schedulerProperties.getScheduler());

		// 输出跨域配置信息
		log.info("跨域配置信息：{}", corsProperties);
	}
}
