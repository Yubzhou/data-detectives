package com.yubzhou.config;

import com.yubzhou.converter.SpelExpressionConverter;
import com.yubzhou.converter.StringToLongTimeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.FormattingConversionService;

/**
 * 此配置类用于注册自定义的 ConversionService，并将其绑定到 Spring Boot 的 ConversionService 上。
 */
@Configuration
public class ConversionConfig {

	private final SpelExpressionConverter spelExpressionConverter;

	@Autowired
	public ConversionConfig(SpelExpressionConverter spelExpressionConverter) {
		this.spelExpressionConverter = spelExpressionConverter;
	}

	@Bean
	@ConfigurationPropertiesBinding // 标记该 ConversionService 专门用于处理 @ConfigurationProperties 的绑定过程。
	public ConversionService conversionService() {
		FormattingConversionService service = new FormattingConversionService();
		ApplicationConversionService.configure(service); // 保留Spring Boot默认转换

		// 注册自定义的 StringToLongTimeConverter
		service.addConverter(new StringToLongTimeConverter());

		// 注册自定义的 SpelExpressionConverter
		service.addConverter(spelExpressionConverter);

		return service;
	}
}