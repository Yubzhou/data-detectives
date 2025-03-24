package com.yubzhou.util;

import java.security.SecureRandom;

public class RandomUtil {
	private static final SecureRandom secureRandom = new SecureRandom();

	/**
	 * 生成指定位数的随机数字
	 *
	 * @param digit 指定位数
	 * @return 指定位数的随机数字
	 */
	public static String generateRandomNumber(int digit) {
		if (digit <= 0) {
			throw new IllegalArgumentException("位数必须大于0");
		}

		// 计算最小值和最大值
		int min = calculateMinValue(digit);
		int max = min * 10 - 1;

		int num = min + secureRandom.nextInt(max - min + 1);

		// 生成随机数字
		return Integer.toString(num);
	}

	/**
	 * 计算指定位数的最小值
	 *
	 * @param digit 位数
	 * @return 最小值
	 */
	private static int calculateMinValue(int digit) {
		int min = 1;
		for (int i = 1; i < digit; i++) {
			min *= 10;
		}
		return min;
	}

	// public static void main(String[] args) {
	// 	// 测试生成指定位数的随机数字
	// 	int digit = 6; // 指定位数
	// 	int randomNumber = generateRandomNumber(digit);
	// 	System.out.println("生成的随机数字是：" + randomNumber);
	// }
}