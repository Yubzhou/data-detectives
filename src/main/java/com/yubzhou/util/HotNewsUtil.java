package com.yubzhou.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HotNewsUtil {

	public static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");
	public static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	// 衰减速率 λ
	public static final double DECAY_1HR = 0;
	public static final double DECAY_24HR = 0.05;
	public static final double DECAY_3DAYS = 0.1;
	public static final double DECAY_5DAYS = 0.15;
	public static final double DECAY_AFTER_5DAYS = 0.2;

	public static String getCurrentHour() {
		return HOUR_FORMATTER.format(LocalDateTime.now());
	}

	public static String getHour(LocalDateTime dateTime) {
		return HOUR_FORMATTER.format(dateTime);
	}

	public static String getCurrentDay() {
		return DAY_FORMATTER.format(LocalDate.now());
	}

	public static String getDay(LocalDate date) {
		return DAY_FORMATTER.format(date);
	}
}