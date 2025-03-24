package com.yubzhou.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReturnCode {
	RC200(200, "请求成功"),
	RC400(400, "请求参数错误或格式不正确"),
	RC401(401, "未授权访问，需要登录或令牌"),
	RC403(403, "无权限访问"),
	RC404(404, "请求资源不存在"),
	RC405(405, "请求方法不被允许"),
	RC409(409, "资源冲突，已存在相同的资源"),
	RC415(415, "不支持的媒体类型"),
	RC429(429, "请求过于频繁，请稍后再试"),
	RC500(500, "服务器内部错误"),
	RC503(503, "服务不可用"),

	INVALID_TOKEN(RC401.code, "无效的token"),
	EXPIRED_TOKEN(RC401.code, "token已过期"),
	TOKEN_GENERATE_ERROR(RC500.code, "token生成失败"),
	ACCESS_DENIED(RC403.code, "无权访问"),
	USERNAME_OR_PASSWORD_ERROR(RC401.code, "账号或密码错误"),
	USER_NOT_FOUND(RC401.code, "账号不存在"),
	USER_ALREADY_EXISTS(RC401.code, "账号已存在"),
	USER_DISABLED(RC401.code, "账号被禁用"),
	USER_LOCKED(RC401.code, "账号被锁定"),
	USER_LOGOUT(RC401.code, "账号已注销");


	private final int code;
	private final String message;
}
