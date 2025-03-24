package com.yubzhou.common;

public class RedisConstant {
	// 以下全部时间单位均为秒

	// 存储refreshToken的前缀
	public static final String USER_REFRESH_TOKEN_PREFIX = "user:refreshToken:";
	// refreshToken有效期
	public static final long USER_REFRESH_TOKEN_EXPIRE_TIME = 5 * 24 * 60 * 60; // 5天


	// 短信验证码限流的前缀（一分钟之内只能发送一次短信验证码）
	public static final String SMS_LIMIT_PREFIX = "login:sms_limit:";
	public static final long SMS_LIMIT_EXPIRE_TIME = 60; // 60秒


	// 存储短信验证码的前缀
	public static final String SMS_CAPTCHA_PREFIX = "login:sms_captcha:";
	// 短信验证码有效期
	public static final long SMS_CAPTCHA_EXPIRE_TIME = 5 * 60; // 5分钟


	// SMS每日限流的前缀
	public static final String SMS_DAILY_LIMIT_PREFIX = "login:sms_daily_limit:";
	// SMS每日限流的有效期
	public static final long SMS_DAILY_LIMIT_EXPIRE_TIME = 24 * 60 * 60; // 1天


	// 登录锁的前缀（检查账号是否被锁定（验证码错误次数限制，如5次错误后锁定1小时））
	public static final String SMS_LOCK_PREFIX = "login:sms_lock:";
	// 登录锁的有效期
	public static final long SMS_LOCK_EXPIRE_TIME = 60 * 60; // 1小时
	// 短信验证码错误次数的前缀
	public static final String SMS_ERROR_COUNT_PREFIX = "login:sms_error_count:";
}
