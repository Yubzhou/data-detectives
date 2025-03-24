package com.yubzhou.common;

public class RegexpConstant {
	/*
	匹配手机号码格式
	正则表达式：^\+\d+-\d+$
	格式示例：+86-13812345678
	 */
	// public static final String PHONE = "^\\+\\d+-\\d+$";
	// public static final String PHONE_MESSAGE = "手机号码格式错误，请输入正确的手机号码";

	/*
	匹配手机号码格式
	正则表达式：^1[3-9]\d{9}$
	格式要求：以1开头，第二位可以是3-9中的一个数字，后面是9位数字。
	格式示例：13812345678
	 */
	public static final String PHONE = "^1[3-9]\\d{9}$";
	public static final String PHONE_MESSAGE = "手机号码格式错误，请输入正确的手机号码";

	/*
	匹配验证码格式
	格式要求：6位数字
	正则表达式：^\d{6}$
	格式示例：123456
	 */
	public static final String CAPTCHA = "^\\d{6}$";
	public static final String CAPTCHA_MESSAGE = "验证码格式错误，请输入6位数字";

	/*
	匹配密码格式
	格式要求：密码长度8-30位，必须包含数字和字母（除空格），可选包含特殊字符，特殊字符包括：!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
	正则表达式：^(?=.*\d)(?=.*[a-zA-Z])[!-~]{8,30}$
	格式示例：Aa123456!@#
	 */
	public static final String PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[!-~]{8,30}$";
	public static final String PASSWORD_MESSAGE = "密码长度8-30位，必须包含数字和字母（除空格），可选包含特殊字符，特殊字符包括：!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
}
