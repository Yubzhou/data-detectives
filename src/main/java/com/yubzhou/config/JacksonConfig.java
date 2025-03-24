package com.yubzhou.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.*;
import com.yubzhou.util.WebContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
@Slf4j
public class JacksonConfig {
	// 定义统一格式（包含毫秒和时区偏移）
	private static final String GLOBAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	private static final DateTimeFormatter GLOBAL_FORMATTER =
			DateTimeFormatter.ofPattern(GLOBAL_DATE_TIME_FORMAT);

	/*
	 * 此配置为全局配置，统一java部分时间相关类型的序列化和反序列化规则
	 * 自定义Jackson ObjectMapperBuilderCustomizer，用于配置ObjectMapper的序列化和反序列化规则
	 * 此配置不会影响其他Jackson配置，如@JsonFormat注解（@JsonFormat注解的优先级高于此配置）
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder -> {
			// 处理Java 8之前的Date类型（JDK7及以下）
			builder.simpleDateFormat(GLOBAL_DATE_TIME_FORMAT);  // 覆盖全局date-format，只对Date类型生效

			// 注册序列化器
			builder.serializers(
					new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")),
					new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
					new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
					new DynamicTimeZoneZonedDateTimeSerializer(),  // 自定义ZonedDateTime序列化器，根据用户中自定义时区请求头动态设置时区（仅处理ZonedDateTime类型）
					new DynamicTimeZoneOffsetDateTimeSerializer(),  // 自定义OffsetDateTime序列化器，根据用户中自定义时区请求头动态设置时区（仅处理OffsetDateTime类型）
					new DynamicTimeZoneInstantSerializer() // 自定义Instant序列化器，根据用户中自定义时区请求头动态设置时区（仅处理Instant类型）
			);

			// 注册JavaTimeModule以支持Java 8时间API的反序列化
			builder.modules(new JavaTimeModule());

			// 禁用时间戳
			builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

			// // 设置时区
			// TimeZone defaultTimeZone = TimeZone.getDefault();  // 获取系统默认时区
			// if (defaultTimeZone == null) {
			// 	defaultTimeZone = TimeZone.getTimeZone("UTC");  // 如果系统默认时区不存在，则设置为UTC时区
			// }
			builder.timeZone(TimeZone.getTimeZone("UTC"));  // 设置时区（设置其他类型时间的时区）
		};
	}

	/**
	 * 自定义ZonedDateTime序列化器，根据用户中自定义时区请求头动态设置时区
	 */
	public static class DynamicTimeZoneZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
		@Override
		public Class<ZonedDateTime> handledType() {  // 显式声明处理 ZonedDateTime 类型
			return ZonedDateTime.class;
		}

		@Override
		public void serialize(ZonedDateTime zonedDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			// 根据用户请求中的自定义时区请求头设置时区
			DateTimeFormatter formatter = GLOBAL_FORMATTER.withZone(WebContextUtil.getTimeZone());
			log.debug("DynamicTimeZoneZonedDateTimeSerializer use time zone: {}", WebContextUtil.getTimeZone());
			jsonGenerator.writeString(formatter.format(zonedDateTime));
		}
	}

	/**
	 * 自定义OffsetDateTime序列化器，根据用户中自定义时区请求头动态设置时区
	 */
	public static class DynamicTimeZoneOffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {
		@Override
		public Class<OffsetDateTime> handledType() {  // 显式声明处理 OffsetDateTime 类型
			return OffsetDateTime.class;
		}

		@Override
		public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			// 根据用户请求中的自定义时区请求头设置时区
			DateTimeFormatter formatter = GLOBAL_FORMATTER.withZone(WebContextUtil.getTimeZone());
			log.debug("DynamicTimeZoneOffsetDateTimeSerializer use time zone: {}", WebContextUtil.getTimeZone());
			jsonGenerator.writeString(formatter.format(offsetDateTime));
		}
	}

	/**
	 * 自定义Instant序列化器，根据用户中自定义时区请求头动态设置时区
	 */
	public static class DynamicTimeZoneInstantSerializer extends JsonSerializer<Instant> {
		@Override
		public Class<Instant> handledType() {  // 显式声明处理 Instant 类型
			return Instant.class;
		}

		@Override
		public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			// 根据用户请求中的自定义时区请求头设置时区
			DateTimeFormatter formatter = GLOBAL_FORMATTER.withZone(WebContextUtil.getTimeZone());
			log.debug("DynamicTimeZoneInstantSerializer use time zone: {}", WebContextUtil.getTimeZone());
			jsonGenerator.writeString(formatter.format(instant));
		}
	}
}