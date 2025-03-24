package com.yubzhou.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Jwt token 有效期单位枚举，用于设置token有效期单位，如：1h、1d、1M等
 * 自定义时间字母单位：y=年，M=月，w=周，d=天，h=小时，m=分钟，s=秒，ms=毫秒
 */
@Getter
@AllArgsConstructor
public enum JwtTimeUnit {
	y(31536000000L), // 365 * 24 * 60 * 60 * 1000，365天
	M(2592000000L), // 30 * 24 * 60 * 60 * 1000，30天
	w(604800000L), // 7 * 24 * 60 * 60 * 1000，7天
	d(86400000L), // 24 * 60 * 60 * 1000，1天
	h(3600000L), // 60 * 60 * 1000，1小时
	m(60000L), // 60 * 1000，1分钟
	s(1000L), // 1000，1秒
	ms(1L); // 1，1毫秒

	// 时间单位转换为毫秒
	private final long millis;
}
