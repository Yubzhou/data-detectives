package com.yubzhou;

import com.yubzhou.common.SystemInfo;
import com.yubzhou.properties.AsyncProperties;
import com.yubzhou.properties.CorsProperties;
import com.yubzhou.properties.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ConfigurationPropertiesScan // 自动扫描并注册所有 @ConfigurationProperties 类
@RequiredArgsConstructor
@Slf4j
public class SpringbootApplication {

	@Value("${spring.kafka.bootstrap-servers}")
	private String kafkaBootstrapServers;

	private final AsyncProperties asyncProperties;
	private final SchedulerProperties schedulerProperties;
	private final CorsProperties corsProperties;

	public static void main(String[] args) {
		SpringApplication.run(SpringbootApplication.class, args);
	}

	// public static void main(String[] args) {
	// 	ConfigurableApplicationContext context = SpringApplication.run(SpringbootApplication.class, args);
	//
	// 	AsyncProperties asyncProperties = context.getBean(AsyncProperties.class);
	// 	SchedulerProperties schedulerProperties = context.getBean(SchedulerProperties.class);
	// 	CorsProperties corsProperties = context.getBean(CorsProperties.class);
	// 	printConfig(asyncProperties, schedulerProperties, corsProperties);
	//
	// 	log.info("系统初始化完成，服务已启动...");
	// }

	// 监听应用启动完成事件
	@EventListener(ApplicationReadyEvent.class)
	public void printConfigAfterStartup() {
		log.info("======= 应用启动完成，输出配置 =======");
		log.info("systemInfo.availableProcessors: {}", SystemInfo.AVAILABLE_PROCESSORS);
		log.info("kafkaBootstrapServers: {}", kafkaBootstrapServers);
		log.info("sse线程池配置: {}", asyncProperties.getSse());
		log.info("上传线程池配置: {}", asyncProperties.getUpload());
		log.info("全局线程池配置: {}", asyncProperties.getGlobal());
		log.info("定时任务线程池配置: {}", schedulerProperties.getScheduler());
		log.info("跨域配置: {}", corsProperties);
	}

	// public void printConfig(AsyncProperties asyncProperties,
	// 							   SchedulerProperties schedulerProperties,
	// 							   CorsProperties corsProperties) {
	// 	// 输出系统可用核心数
	// 	log.info("systemInfo.availableProcessors: {}", SystemInfo.AVAILABLE_PROCESSORS);
	//
	// 	// 输出 kafka 配置信息
	// 	log.info("kafkaBootstrapServers: {}", kafkaBootstrapServers);
	//
	// 	// 输出线程池配置信息
	// 	log.info("sse线程池配置信息：{}", asyncProperties.getSse());
	// 	log.info("上传线程池配置信息：{}", asyncProperties.getUpload());
	// 	log.info("全局线程池配置信息：{}", asyncProperties.getGlobal());
	//
	// 	// 输出定时任务配置信息
	// 	log.info("定时任务线程池配置信息：{}", schedulerProperties.getScheduler());
	//
	// 	// 输出跨域配置信息
	// 	log.info("跨域配置信息：{}", corsProperties);
	// }
}
