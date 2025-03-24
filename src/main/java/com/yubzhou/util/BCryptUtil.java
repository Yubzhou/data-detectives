package com.yubzhou.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密工具类
 */
public class BCryptUtil {

	private static final BCryptPasswordEncoder passwordEncoder;

	static {
		passwordEncoder = new BCryptPasswordEncoder();
	}

	/**
	 * 加密密码
	 */
	public static String encode(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	/**
	 * 校验密码
	 */
	public static boolean matches(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}

	// public static void main(String[] args) {
	// 	BCryptUtil bCryptUtil = new BCryptUtil();
	// 	String rawPassword = "123456";
	// 	String encodedPassword = bCryptUtil.encode(rawPassword);
	// 	System.out.println("加密后的密码: " + encodedPassword);
	// 	System.out.println("加密后的密码的长度: " + encodedPassword.length());
	// 	System.out.println("校验密码: " + bCryptUtil.matches(rawPassword, encodedPassword));
	// }
}