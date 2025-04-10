package com.yubzhou;

import com.yubzhou.common.SystemInfo;
import com.yubzhou.properties.AsyncProperties;
import com.yubzhou.properties.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
@ConfigurationPropertiesScan // 自动扫描并注册所有 @ConfigurationProperties 类
public class SpringbootApplication {

	public static void main(String[] args) {
		// 设置系统属性
		System.setProperty("systemInfo.availableProcessors", Integer.toString(SystemInfo.AVAILABLE_PROCESSORS));
		ConfigurableApplicationContext context = SpringApplication.run(SpringbootApplication.class, args);

		AsyncProperties asyncProperties = context.getBean(AsyncProperties.class);
		CorsProperties corsProperties = context.getBean(CorsProperties.class);
		test(asyncProperties, corsProperties);
	}

	public static void test(AsyncProperties asyncProperties, CorsProperties corsProperties) {
		System.out.println(asyncProperties.getSse());
		System.out.println(asyncProperties.getUpload());
		System.out.println(asyncProperties.getGlobal());

		System.out.println(Arrays.toString(corsProperties.getAllowedOriginPatterns()));
	}
}
