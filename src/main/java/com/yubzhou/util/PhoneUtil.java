package com.yubzhou.util;

public class PhoneUtil {
	// 判断手机号是否属于中国移动的手机号码段
	private static final int[][] CHINA_MOBILE_PHONE_SECTIONS = {
			{134, 139},
			{147, 147},
			{150, 152},
			{157, 159},
			{165, 165},
			{172, 172},
			{178, 178},
			{182, 184},
			{187, 188},
			{195, 195},
			{197, 198}
	};

	public static boolean isChinaMobile(String phone) {
		// 验证手机号格式
		if (phone == null) {
			return false;
		}
		phone = phone.trim();
		if (phone.length() < 11) {
			return false;
		}

		// 提取前三位并转为整数
		String prefixStr = phone.substring(0, 3);
		try {
			int prefix = Integer.parseInt(prefixStr);
			// 检查是否在任一范围内
			for (int[] range : CHINA_MOBILE_PHONE_SECTIONS) {
				if (prefix >= range[0] && prefix <= range[1]) {
					return true;
				}
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return false;
	}

	// public static void main(String[] args) {
	// 	// 测试示例
	// 	String[] testNumbers = {
	// 			"13912345678",  // 属于134-139段
	// 			"14700000000",  // 属于147段
	// 			"15255556666",  // 属于150-152段
	// 			"16512341234",  // 属于165段
	// 			"17887654321",  // 属于178段
	// 			"18400000000",  // 属于182-184段
	// 			"18812345678",  // 属于187-188段
	// 			"19876543210",  // 属于197-198段
	// 			"12345678901",  // 无效前三位
	// 			"11122223333"   // 无效格式（假设这里故意错误）
	// 	};
	// 	long startTime = System.currentTimeMillis();
	// 	for (String number : testNumbers) {
	// 		System.out.println(number + " 是否有效: " + isChinaMobile(number));
	// 	}
	// 	long endTime = System.currentTimeMillis();
	// 	System.out.println("总耗时: " + (endTime - startTime) + " ms");
	// }
}
