package com.yubzhou.util;

import com.yubzhou.common.UserToken;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;


public class WebContextUtil {
	private static final String USER_TOKEN_KEY = "userToken";
	private static final String ACCESS_TOKEN_KEY = "accessToken";
	private static final String REFRESH_TOKEN_KEY = "refreshToken";
	private static final String TIME_ZONE_KEY = "X-Time-Zone";
	public static final String TIME_ZONE_HEADER = TIME_ZONE_KEY;

	// 本地线程缓存token
	private static final ThreadLocal<Map<String, Object>> CONTEXT_LOCAL = new ThreadLocal<>();

	private static final ZoneId UTC = ZoneId.of("UTC");

	/**
	 * 获取token信息并将一些自定义声明封装到UserToken中，并存入到本地线程缓存中
	 *
	 * @param userToken token中用户相关的自定义声明
	 */
	public static void setUserToken(UserToken userToken) {
		setKey(USER_TOKEN_KEY, userToken);
	}

	/**
	 * 获取本地线程缓存中的token信息
	 *
	 * @return UserToken token中用户相关的自定义声明
	 */
	public static UserToken getUserToken() {
		return getKey(USER_TOKEN_KEY, UserToken.class);
	}

	public static void setAccessToken(String accessToken) {
		setKey(ACCESS_TOKEN_KEY, accessToken);
	}

	public static String getAccessToken() {
		return getKey(ACCESS_TOKEN_KEY, String.class);
	}

	public static void setRefreshToken(String refreshToken) {
		setKey(REFRESH_TOKEN_KEY, refreshToken);
	}

	public static String getRefreshToken() {
		return getKey(REFRESH_TOKEN_KEY, String.class);
	}

	public static void setTimeZone(ZoneId zoneId) {
		setKey(TIME_ZONE_KEY, zoneId);
	}

	public static ZoneId getTimeZone() {
		ZoneId zoneId = getKey(TIME_ZONE_KEY, ZoneId.class);
		return zoneId != null ? zoneId : UTC;
	}

	/**
	 * 移除本地线程缓存中的token信息
	 */
	public static void removeContext() {
		CONTEXT_LOCAL.remove();
	}

	/**
	 * 设置本地线程缓存中的key-value信息
	 */
	private static void setKey(String key, Object value) {
		Map<String, Object> context = CONTEXT_LOCAL.get();
		if (context == null) {
			context = new HashMap<>();
			CONTEXT_LOCAL.set(context);
		}
		context.put(key, value);
	}

	/**
	 * 获取本地线程缓存中的key-value信息
	 */
	private static <T> T getKey(String key, Class<T> clazz) {
		Map<String, Object> context = CONTEXT_LOCAL.get();
		if (context == null) return null;
		// 获取key对应的value
		Object value = context.get(key);
		if (value == null) return null;
		// 将Object转换为指定的类型T
		return clazz.cast(value);
	}
}