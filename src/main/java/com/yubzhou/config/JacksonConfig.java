package com.yubzhou.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.yubzhou.util.DateTimeUtil;
import com.yubzhou.util.WebContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.TimeZone;

@Configuration
@Slf4j
public class JacksonConfig {

	/**
	 * 业务专用的ObjectMapper，不继承全局配置
	 */
	@Bean(name = "businessObjectMapper")
	public ObjectMapper businessObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();

		// 注册 JavaTimeModule（处理 Java 8 时间类型）
		JavaTimeModule javaTimeModule = new JavaTimeModule();

		// 序列化配置
		javaTimeModule.addSerializer(new LocalDateTimeSerializer(DateTimeUtil.LOCAL_DATE_TIME_NO_MILLIS_FORMATTER));
		javaTimeModule.addSerializer(new LocalDateSerializer(DateTimeUtil.LOCAL_DATE_FORMATTER));
		javaTimeModule.addSerializer(new LocalTimeSerializer(DateTimeUtil.LOCAL_TIME_NO_MILLIS_FORMATTER));

		// 反序列化配置
		javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeUtil.LOCAL_DATE_TIME_NO_MILLIS_FORMATTER));
		javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeUtil.LOCAL_DATE_FORMATTER));
		javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeUtil.LOCAL_TIME_NO_MILLIS_FORMATTER));

		// -------------------- 自定义序列化器 --------------------
		// ZonedDateTime
		javaTimeModule.addSerializer(
				ZonedDateTime.class,
				new JsonSerializer<>() {
					@Override
					public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider provider)
							throws IOException {
						gen.writeString(value.format(DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER));
					}
				}
		);

		// OffsetDateTime
		javaTimeModule.addSerializer(
				OffsetDateTime.class,
				new JsonSerializer<>() {
					@Override
					public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider provider)
							throws IOException {
						gen.writeString(value.format(DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER));
					}
				}
		);

		// Instant（转为时间戳，或按格式字符串）
		javaTimeModule.addSerializer(
				Instant.class,
				new JsonSerializer<>() {
					@Override
					public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider)
							throws IOException {
						// gen.writeNumber(value.toEpochMilli()); // 时间戳
						// 或按格式字符串：
						// gen.writeString(value.atZone(ZoneId.systemDefault()).format(DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER));
						gen.writeString(DateTimeUtil.format(value, DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER));
					}
				}
		);

		// -------------------- 自定义反序列化器 --------------------
		// ZonedDateTime
		javaTimeModule.addDeserializer(
				ZonedDateTime.class,
				new JsonDeserializer<>() {
					@Override
					public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctx)
							throws IOException {
						return ZonedDateTime.parse(p.getText(), DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER);
					}
				}
		);

		// OffsetDateTime
		javaTimeModule.addDeserializer(
				OffsetDateTime.class,
				new JsonDeserializer<>() {
					@Override
					public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctx)
							throws IOException {
						return OffsetDateTime.parse(p.getText(), DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER);
					}
				}
		);

		// Instant（从时间戳解析）
		javaTimeModule.addDeserializer(
				Instant.class,
				new JsonDeserializer<>() {
					@Override
					public Instant deserialize(JsonParser p, DeserializationContext ctx)
							throws IOException {
						// return Instant.ofEpochMilli(p.getLongValue());
						// 或从字符串解析：
						// return LocalDateTime.parse(p.getText(), DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER).atZone(ZoneId.systemDefault()).toInstant();
						return DateTimeUtil.parseInstant(p.getText(), DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER);
					}
				}
		);

		// 注册模块
		mapper.registerModule(javaTimeModule);

		// 处理旧版 Date 类型
		mapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMAT));
		// 禁用时间戳格式
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		return mapper;
	}

	/**
	 * 默认的全局ObjectMapper，标记为@Primary
	 */
	@Bean
	@Primary // 标记为@Primary
	public ObjectMapper defaultObjectMapper(Jackson2ObjectMapperBuilder builder) {
		return builder.build();
	}

	/*
	 * 此配置为全局配置，统一java部分时间相关类型的序列化和反序列化规则
	 * 自定义Jackson ObjectMapperBuilderCustomizer，用于配置ObjectMapper的序列化和反序列化规则
	 * 此配置不会影响其他Jackson配置，如@JsonFormat注解（@JsonFormat注解的优先级高于此配置）
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder -> {
			// 处理Java 8之前的Date类型（JDK7及以下）
			builder.simpleDateFormat(DateTimeUtil.GLOBAL_DATE_TIME_FORMAT);  // 覆盖全局date-format，只对Date类型生效

			// 注册序列化器
			builder.serializers(
					new LocalDateTimeSerializer(DateTimeUtil.LOCAL_DATE_TIME_FORMATTER),
					new LocalDateSerializer(DateTimeUtil.LOCAL_DATE_FORMATTER),
					new LocalTimeSerializer(DateTimeUtil.LOCAL_TIME_FORMATTER),
					new DynamicTimeZoneZonedDateTimeSerializer(),  // 自定义ZonedDateTime序列化器，根据用户中自定义时区请求头动态设置时区（仅处理ZonedDateTime类型）
					new DynamicTimeZoneOffsetDateTimeSerializer(),  // 自定义OffsetDateTime序列化器，根据用户中自定义时区请求头动态设置时区（仅处理OffsetDateTime类型）
					new DynamicTimeZoneInstantSerializer() // 自定义Instant序列化器，根据用户中自定义时区请求头动态设置时区（仅处理Instant类型）
			);

			// 注册反序列化器
			builder.deserializers(
					new LocalDateTimeDeserializer(DateTimeUtil.LOCAL_DATE_TIME_FORMATTER),
					new LocalDateDeserializer(DateTimeUtil.LOCAL_DATE_FORMATTER),
					new LocalTimeDeserializer(DateTimeUtil.LOCAL_TIME_FORMATTER),
					new DynamicTimeZoneZonedDateTimeDeserializer(),  // 自定义ZonedDateTime反序列化器，根据用户中自定义时区请求头动态设置时区（仅处理ZonedDateTime类型）
					new DynamicTimeZoneOffsetDateTimeDeserializer(),  // 自定义OffsetDateTime反序列化器，根据用户中自定义时区请求头动态设置时区（仅处理OffsetDateTime类型）
					new DynamicTimeZoneInstantDeserializer() // 自定义Instant反序列化器，根据用户中自定义时区请求头动态设置时区（仅处理Instant类型）
			);

			// 注册JavaTimeModule以支持Java 8时间API的反序列化
			builder.modules(new JavaTimeModule());

			// 禁用时间戳
			builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

			// 设置时区
			TimeZone defaultTimeZone = TimeZone.getDefault();  // 获取系统默认时区
			if (defaultTimeZone == null) {
				defaultTimeZone = TimeZone.getTimeZone("UTC");  // 如果系统默认时区不存在，则设置为UTC时区
			}
			builder.timeZone(defaultTimeZone);
			// builder.timeZone(TimeZone.getTimeZone("UTC"));  // 设置时区（设置其他类型时间的时区）
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
			jsonGenerator.writeString(DateTimeUtil.format(zonedDateTime, WebContextUtil.getTimeZone()));
			log.debug("DynamicTimeZoneZonedDateTimeSerializer use time zone: {}", WebContextUtil.getTimeZone());
		}
	}

	/**
	 * 自定义ZonedDateTime反序列化器，根据用户中自定义时区请求头动态设置时区
	 */
	public static class DynamicTimeZoneZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
		@Override
		public Class<ZonedDateTime> handledType() {  // 显式声明处理 ZonedDateTime 类型
			return ZonedDateTime.class;
		}

		@Override
		public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			// 根据用户请求中的自定义时区请求头设置时区
			return DateTimeUtil.parseZonedDateTime(jsonParser.getText(), WebContextUtil.getTimeZone());
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
			jsonGenerator.writeString(DateTimeUtil.format(offsetDateTime, WebContextUtil.getTimeZone()));
			log.debug("DynamicTimeZoneOffsetDateTimeSerializer use time zone: {}", WebContextUtil.getTimeZone());
		}
	}

	/**
	 * 自定义OffsetDateTime反序列化器，根据用户中自定义时区请求头动态设置时区
	 */
	public static class DynamicTimeZoneOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
		@Override
		public Class<OffsetDateTime> handledType() {  // 显式声明处理 OffsetDateTime 类型
			return OffsetDateTime.class;
		}

		@Override
		public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			// 根据用户请求中的自定义时区请求头设置时区
			return DateTimeUtil.parseOffsetDateTime(jsonParser.getText(), WebContextUtil.getTimeZone());
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
			jsonGenerator.writeString(DateTimeUtil.format(instant, WebContextUtil.getTimeZone()));
			log.debug("DynamicTimeZoneInstantSerializer use time zone: {}", WebContextUtil.getTimeZone());
		}
	}

	/**
	 * 自定义Instant反序列化器，根据用户中自定义时区请求头动态设置时区
	 */
	public static class DynamicTimeZoneInstantDeserializer extends JsonDeserializer<Instant> {
		@Override
		public Class<Instant> handledType() {  // 显式声明处理 Instant 类型
			return Instant.class;
		}

		@Override
		public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			// 根据用户请求中的自定义时区请求头设置时区
			return DateTimeUtil.parseInstant(jsonParser.getText(), WebContextUtil.getTimeZone());
		}
	}
}