package com.yubzhou.util;

import lombok.NonNull;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
	public static final String GLOBAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"; // 定义统一格式（包含毫秒和时区偏移）
	public static final String GLOBAL_DATE_TIME_NO_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
	public static final String LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String LOCAL_DATE_TIME_NO_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String LOCAL_DATE_FORMAT = "yyyy-MM-dd";
	public static final String LOCAL_TIME_FORMAT = "HH:mm:ss.SSS";
	public static final String LOCAL_TIME_NO_MILLIS_FORMAT = "HH:mm:ss";
	public static final DateTimeFormatter GLOBAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(GLOBAL_DATE_TIME_FORMAT);
	public static final DateTimeFormatter GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER = DateTimeFormatter.ofPattern(GLOBAL_DATE_TIME_NO_MILLIS_FORMAT);
	public static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT);
	public static final DateTimeFormatter LOCAL_DATE_TIME_NO_MILLIS_FORMATTER =
			DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_NO_MILLIS_FORMAT);
	public static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT);
	public static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_TIME_FORMAT);
	public static final DateTimeFormatter LOCAL_TIME_NO_MILLIS_FORMATTER =
			DateTimeFormatter.ofPattern(LOCAL_TIME_NO_MILLIS_FORMAT);

	// ===================================== 格式化时间 =========================================

	// 按默认格式化器 格式化 LocalDateTime
	public static String format(LocalDateTime localDateTime) {
		return format(localDateTime, LOCAL_DATE_TIME_FORMATTER);
	}

	// 按指定格式化器 格式化 LocalDateTime
	public static String format(LocalDateTime localDateTime, DateTimeFormatter formatter) {
		// 内部会判断 formatter 是否为空
		return localDateTime.format(formatter);
	}

	// 按默认格式化器 格式化 LocalDate
	public static String format(LocalDate localDate) {
		return format(localDate, LOCAL_DATE_FORMATTER);
	}

	// 按指定格式化器 格式化 LocalDate
	public static String format(LocalDate localDate, DateTimeFormatter formatter) {
		// 内部会判断 formatter 是否为空
		return localDate.format(formatter);
	}

	// 按默认格式化器 格式化 LocalTime
	public static String format(LocalTime localTime) {
		return format(localTime, LOCAL_TIME_FORMATTER);
	}

	// 按指定格式化器 格式化 LocalTime
	public static String format(LocalTime localTime, DateTimeFormatter formatter) {
		// 内部会判断 formatter 是否为空
		return localTime.format(formatter);
	}

	// 按默认格式化器 格式化 ZonedDateTime
	public static String format(ZonedDateTime zonedDateTime) {
		return format(zonedDateTime, GLOBAL_DATE_TIME_FORMATTER);
	}

	// 按指定格式化器 格式化 ZonedDateTime
	public static String format(ZonedDateTime zonedDateTime, DateTimeFormatter formatter) {
		// 内部会判断 formatter 是否为空
		return zonedDateTime.format(formatter);
	}

	// 按默认格式化器和指定时区 格式化 ZonedDateTime
	public static String format(ZonedDateTime zonedDateTime, ZoneId zoneId) {
		return format(zonedDateTime, GLOBAL_DATE_TIME_FORMATTER, zoneId);
	}

	// 按指定格式化器和指定时区 格式化 ZonedDateTime
	public static String format(ZonedDateTime zonedDateTime, DateTimeFormatter formatter, @NonNull ZoneId zoneId) {
		return zonedDateTime.format(formatter.withZone(zoneId));
	}

	// 按默认格式化器 格式化 OffsetDateTime
	public static String format(OffsetDateTime offsetDateTime) {
		return format(offsetDateTime, GLOBAL_DATE_TIME_FORMATTER);
	}

	// 按指定格式化器 格式化 OffsetDateTime
	public static String format(OffsetDateTime offsetDateTime, DateTimeFormatter formatter) {
		// 内部会判断 formatter 是否为空
		return offsetDateTime.format(formatter);
	}

	// 按默认格式化器和指定时区 格式化 OffsetDateTime
	public static String format(OffsetDateTime offsetDateTime, ZoneId zoneId) {
		return format(offsetDateTime, GLOBAL_DATE_TIME_FORMATTER, zoneId);
	}

	// 按指定格式化器和指定时区 格式化 OffsetDateTime
	public static String format(OffsetDateTime offsetDateTime, DateTimeFormatter formatter, @NonNull ZoneId zoneId) {
		return offsetDateTime.format(formatter.withZone(zoneId));
	}

	// 按默认格式化器 格式化 Instant
	public static String format(Instant instant) {
		return format(instant, GLOBAL_DATE_TIME_FORMATTER);
	}

	// 按指定格式化器 格式化 Instant
	public static String format(Instant instant, @NonNull DateTimeFormatter formatter) {
		return formatter.format(instant.atOffset(ZoneOffset.UTC));
	}

	// 按默认格式化器和指定时区 格式化 Instant
	public static String format(Instant instant, ZoneId zoneId) {
		return format(instant, GLOBAL_DATE_TIME_FORMATTER, zoneId);
	}

	// 按指定格式化器和指定时区 格式化 Instant
	public static String format(Instant instant, @NonNull DateTimeFormatter formatter, @NonNull ZoneId zoneId) {
		return formatter.withZone(zoneId).format(instant.atOffset(ZoneOffset.UTC));
	}


	// ===================================== 解析字符串 =========================================

	// 按默认格式化器 解析字符串为 LocalDateTime
	public static LocalDateTime parseLocalDateTime(String localDateTime) {
		return parseLocalDateTime(localDateTime, LOCAL_DATE_TIME_FORMATTER);
	}

	// 按无毫秒格式化器 解析字符串为 LocalDateTime
	public static LocalDateTime parseLocalDateTimeNoMillis(String localDateTime) {
		return parseLocalDateTime(localDateTime, LOCAL_DATE_TIME_NO_MILLIS_FORMATTER);
	}

	// 按指定格式化器 解析字符串为 LocalDateTime
	public static LocalDateTime parseLocalDateTime(String localDateTime, DateTimeFormatter formatter) {
		return LocalDateTime.parse(localDateTime, formatter);
	}

	// 按默认格式化器 解析字符串为 LocalDate
	public static LocalDate parseLocalDate(String localDate) {
		return parseLocalDate(localDate, LOCAL_DATE_FORMATTER);
	}

	// 按指定格式化器 解析字符串为 LocalDate
	public static LocalDate parseLocalDate(String localDate, DateTimeFormatter formatter) {
		return LocalDate.parse(localDate, formatter);
	}

	// 按默认格式化器 解析字符串为 LocalTime
	public static LocalTime parseLocalTime(String localTime) {
		return parseLocalTime(localTime, LOCAL_TIME_FORMATTER);
	}

	// 按无毫秒格式化器 解析字符串为 LocalTime
	public static LocalTime parseLocalTimeNoMillis(String localTime) {
		return parseLocalTime(localTime, LOCAL_TIME_NO_MILLIS_FORMATTER);
	}

	// 按指定格式化器 解析字符串为 LocalTime
	public static LocalTime parseLocalTime(String localTime, DateTimeFormatter formatter) {
		return LocalTime.parse(localTime, formatter);
	}

	// 按默认格式化器 解析字符串为 ZonedDateTime
	public static ZonedDateTime parseZonedDateTime(String zonedDateTime) {
		return parseZonedDateTime(zonedDateTime, GLOBAL_DATE_TIME_FORMATTER);
	}

	// 按指定格式化器 解析字符串为 ZonedDateTime
	public static ZonedDateTime parseZonedDateTime(String zonedDateTime, DateTimeFormatter formatter) {
		return ZonedDateTime.parse(zonedDateTime, formatter);
	}

	// 按默认格式化器和指定时区 解析字符串为 ZonedDateTime
	public static ZonedDateTime parseZonedDateTime(String zonedDateTime, ZoneId zoneId) {
		return parseZonedDateTime(zonedDateTime, GLOBAL_DATE_TIME_FORMATTER, zoneId);
	}

	// 按指定格式化器和指定时区 解析字符串为 ZonedDateTime
	public static ZonedDateTime parseZonedDateTime(String zonedDateTime, DateTimeFormatter formatter, @NonNull ZoneId zoneId) {
		return ZonedDateTime.parse(zonedDateTime, formatter.withZone(zoneId));
	}

	// 按默认格式化器 解析字符串为 OffsetDateTime
	public static OffsetDateTime parseOffsetDateTime(String offsetDateTime) {
		return parseOffsetDateTime(offsetDateTime, GLOBAL_DATE_TIME_FORMATTER);
	}

	// 按指定格式化器 解析字符串为 OffsetDateTime
	public static OffsetDateTime parseOffsetDateTime(String offsetDateTime, DateTimeFormatter formatter) {
		return OffsetDateTime.parse(offsetDateTime, formatter);
	}

	// 按默认格式化器和指定时区 解析字符串为 OffsetDateTime
	public static OffsetDateTime parseOffsetDateTime(String offsetDateTime, ZoneId zoneId) {
		return parseOffsetDateTime(offsetDateTime, GLOBAL_DATE_TIME_FORMATTER, zoneId);
	}

	// 按指定格式化器和指定时区 解析字符串为 OffsetDateTime
	public static OffsetDateTime parseOffsetDateTime(String offsetDateTime, DateTimeFormatter formatter, @NonNull ZoneId zoneId) {
		return OffsetDateTime.parse(offsetDateTime, formatter.withZone(zoneId));
	}

	// 按默认格式化器 解析字符串为 Instant
	public static Instant parseInstant(String instant) {
		return parseInstant(instant, GLOBAL_DATE_TIME_FORMATTER);
	}

	// 按指定格式化器 解析字符串为 Instant
	public static Instant parseInstant(String instant, @NonNull DateTimeFormatter formatter) {
		return OffsetDateTime.parse(instant, formatter).toInstant();
	}

	// 按默认格式化器和指定时区 解析字符串为 Instant
	public static Instant parseInstant(String instant, ZoneId zoneId) {
		return parseInstant(instant, GLOBAL_DATE_TIME_FORMATTER, zoneId);
	}

	// 按指定格式化器和指定时区 解析字符串为 Instant
	public static Instant parseInstant(String instant, @NonNull DateTimeFormatter formatter, @NonNull ZoneId zoneId) {
		return formatter.withZone(zoneId).parse(instant, OffsetDateTime::from).toInstant();
	}
}
