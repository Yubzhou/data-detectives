package com.yubzhou.util;

import com.yubzhou.common.UserActionEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class HotNewsUtil {

	public static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");
	public static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	// 基础热度的最小保留比例（建议 0.05~0.2）
	private static final double MIN_RATE = 0.05;

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

	/**
	 * 基于分段线性插值的平滑函数实现，提供连续的热度衰减率，避免阶梯式跳跃
	 *
	 * @param deltaDays 时间差，单位：天
	 * @return 返回热度衰减率
	 */
	public static double calculateDecayRate(double deltaDays) {
		// 定义关键时间点和对应的衰减率
		final double[] thresholds = {1.0, 3.0, 5.0, 7.0}; // 时间阈值，单位：天
		final double[] rates = {0.005, 0.015, 0.03, 0.05}; // 衰减速率 λ

		// 小于1天无衰减
		if (deltaDays < 1.0) {
			return 0.0;
		}
		// 大于7天使用最大衰减率
		else if (deltaDays >= 7.0) {
			return rates[3];
		}

		// 寻找当前所处区间
		int segment = 0;
		while (segment < thresholds.length && deltaDays >= thresholds[segment]) {
			segment++;
		}
		segment = Math.min(segment, thresholds.length - 1);

		// 计算线性插值
		double prevThreshold = (segment == 0) ? 1.0 : thresholds[segment - 1];
		double nextThreshold = thresholds[segment];
		double prevRate = (segment == 0) ? 0.0 : rates[segment - 1];
		double nextRate = rates[segment];

		double ratio = (deltaDays - prevThreshold) / (nextThreshold - prevThreshold);
		return prevRate + ratio * (nextRate - prevRate);
	}

	// 基础热度计算
	public static double calculateBaseHotness(Map<Object, Object> metrics) {
		return UserActionEvent.ActionType.calculateHotScore(metrics);
	}

	/**
	 * 时间衰减计算
	 * 计算公式举例：热度 = [1.5 × log(浏览量+1) + 0.25 × 点赞数 + 0.35 × 评论数 + 0.25 × 收藏数] × (minRate + (1−minRate) × e^(−λt))
	 * Hotness = Base × (minRate + (1−minRate) × e^(−λt))
	 * 其中λ为衰减速率，t为时间间隔（单位：天）
	 */
	public static double calculateDecayedHotness(double base, LocalDateTime createdAt) {
		// ChronoUnit.HOURS.between 方法参数1为起始时间点，参数2为结束时间点，其返回值为 参数2-参数1，即时间差（单位：小时）
		long deltaHours = ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());
		double deltaDays = Math.abs(deltaHours) / 24.0;
		// System.out.println(deltaDays);
		// 获取衰减速率λ（使用平滑函数）
		double lambda = calculateDecayRate(deltaDays);
		// return base * Math.exp(-lambda * deltaDays); // λ=lambda, t=deltaDays
		// 带渐进下限的衰减公式
		return base * (MIN_RATE + (1 - MIN_RATE) * Math.exp(-lambda * deltaDays));
	}

	public static void main(String[] args) {
		System.out.println(calculateDecayedHotness(170.36, LocalDateTime.now().minusDays(3)));
	}

	// public static void main(String[] args) {
	// 	// test01();
	// 	test02();
	// 	// test03();
	// }
	//
	// public static void test01() {
	// 	System.out.println("0 = " + calculateDecayRate(0));
	// 	System.out.println("1 = " + calculateDecayRate(1));
	// 	System.out.println("2 = " + calculateDecayRate(2));
	// 	System.out.println("2.5 = " + calculateDecayRate(2.5));
	// 	System.out.println("3 = " + calculateDecayRate(3));
	// 	System.out.println("4 = " + calculateDecayRate(4));
	// 	System.out.println("4.5 = " + calculateDecayRate(4.5));
	// 	System.out.println("5 = " + calculateDecayRate(5));
	// 	System.out.println("6 = " + calculateDecayRate(6));
	// 	System.out.println("6.5 = " + calculateDecayRate(6.5));
	// 	System.out.println("7 = " + calculateDecayRate(7));
	// 	System.out.println("7.5 = " + calculateDecayRate(7.5));
	// 	System.out.println("8 = " + calculateDecayRate(8));
	// }
	//
	// public static void test02() {
	// 	int base = 500;
	// 	System.out.println("0 = " + calculateDecayedHotness(base, LocalDateTime.now())); // 小于1天
	// 	System.out.println("1 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(1))); // 小于3天
	// 	System.out.println("4 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(4))); // 小于5天
	// 	System.out.println("6 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(6))); // 小于7天
	// 	System.out.println("8 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(8))); // 大于7天
	// 	System.out.println("10 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(10))); // 大于7天
	// 	System.out.println("20 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(20))); // 大于7天
	// 	System.out.println("50 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(50))); // 大于7天
	// 	System.out.println("100 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(100))); // 大于7天
	// 	System.out.println("500 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(500))); // 大于7天
	// 	System.out.println("1000 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(1000))); // 大于7天
	// }
	//
	//
	// public static void test03() {
	// 	int base = 1000;
	// 	long startTime = System.currentTimeMillis();
	// 	System.out.println("1000 = " + calculateDecayedHotness(base, LocalDateTime.now().plusDays(1000))); // 大于7天
	// 	System.out.println("耗时：" + (System.currentTimeMillis() - startTime) + "ms");
	// }
}
