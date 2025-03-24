package com.yubzhou.converter;

import com.yubzhou.properties.JwtTimeUnit;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用于将字符串转换为长时间值（单位：ms）的转换器，来自定义解析配置文件的配置项。
 * 支持格式：数字后加单位：1000ms、10s、1h、1d、1M、1y（一定要同时有数字和单位）
 */
public class StringToLongTimeConverter implements Converter<String, Long> {

	// 正则表达式匹配数值和时间单位（优先匹配ms）
	private static final Pattern TIME_PATTERN;

	static {
		TIME_PATTERN = generateTimePattern();
	}

	@Override
	public Long convert(String source) {
		Matcher matcher = TIME_PATTERN.matcher(source);
		if (matcher.matches()) {
			long value = Long.parseLong(matcher.group(1));
			String unit = matcher.group(2);

			// 获取对应的枚举值
			JwtTimeUnit timeUnit = JwtTimeUnit.valueOf(unit);
			// 转换为毫秒值
			return value * timeUnit.getMillis();
		}
		return Long.parseLong(source);
	}

	/**
	 * 依据JwtTimeUnit枚举类中的全部枚举常量来生成对应的正则表达式
	 *
	 * @return 正则表达式（如：^\\d+(ms|y|M|w|d|h|m|s)$）
	 */
	private static Pattern generateTimePattern() {
		// 获取所有枚举常量的名称，用"|"分隔拼接为字符串（如："y|M|w|d|h|m|s|ms"）
		String timeUnits = Arrays.stream(JwtTimeUnit.values())
				.map(Enum::name)
				.collect(Collectors.joining("|"));

		// 构建正则表达式
		String regex = "^(\\d+)(" + timeUnits + ")$";

		// 将正则表达式编译为Pattern对象
		return Pattern.compile(regex);
	}
}