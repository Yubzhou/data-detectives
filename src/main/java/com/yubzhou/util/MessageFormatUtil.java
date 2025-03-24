package com.yubzhou.util;

import java.text.MessageFormat;

public class MessageFormatUtil {
	/**
	 * 格式化消息（替换占位符 {0}, {1}...）
	 */
	public static String formatMessage(String template, Object[] args) {
		return args != null && args.length > 0
				? MessageFormat.format(template, args)
				: template;
	}
}
