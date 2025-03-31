package com.yubzhou.util;

import com.yubzhou.common.ReturnCode;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.exception.TokenInvalidException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class LocalAssert {
	public static void hasText(String str, String message) {
		hasText(str, ReturnCode.RC400.getCode(), message);
	}

	public static void hasText(String str, int code, String message) {
		if (!StringUtils.hasText(str)) {
			throw new BusinessException(code, message);
		}
	}

	// 判断一个数是否为正数
	public static void assertPositive(int num, String message) {
		if (num <= 0) {
			throw new BusinessException(ReturnCode.RC400.getCode(), message);
		}
	}

	public static final Pattern smsCodePattern = Pattern.compile("^\\d{6}$");
	// 判断短信验证码格式是否正确
	public static void assertSmsCodeFormat(String code, String message) {
		if (code == null || !smsCodePattern.matcher(code).matches()) {
			throw new BusinessException(ReturnCode.RC400.getCode(), message);
		}
	}

	// 判断token是否为空
	public static void assertTokenHasText(String token, String message) {
		if (!StringUtils.hasText(token)) {
			throw new TokenInvalidException(message);
		}
	}
}
