package com.yubzhou.exception;

import com.yubzhou.common.ReturnCode;
import lombok.Getter;

@Getter
public class TokenInvalidException extends RuntimeException {

	// 响应头，作为响应前端的响应头
	public static final String TOKEN_INVALID_HEADER = "x-token-invalid";

	// 错误码，作为响应前端的响应状态码
	private final int code;

	/**
	 * 构造方法
	 * 默认响应状态码为401
	 *
	 * @param message 错误信息
	 */
	public TokenInvalidException(String message) {
		super(message);
		this.code = ReturnCode.INVALID_TOKEN.getCode();
	}
}