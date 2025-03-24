package com.yubzhou.exception;

import com.yubzhou.common.ReturnCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 自定义业务异常（需继承 RuntimeException）
 */
@Getter
public class BusinessException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 7266600526367962223L;

	// 错误码
	private final int code;
	// 错误信息中的动态参数（用于消息模板中的占位符替换）
	private final Object[] args;

	/**
	 * 构造函数
	 *
	 * @param rc ReturnCode枚举
	 */
	public BusinessException(ReturnCode rc) {
		this(rc.getCode(), rc.getMessage());
	}

	/**
	 * 构造函数
	 *
	 * @param code     http响应码
	 * @param template 错误信息模板（比如“用户{}不存在”）
	 */
	public BusinessException(int code, String template) {
		this(code, template, new Object[0]);
	}

	/**
	 * 构造函数
	 *
	 * @param code     http响应码
	 * @param template 错误信息模板（比如“用户{0}不存在”），使用Java MessageFormat风格的占位符
	 * @param args     错误信息模板中的动态参数
	 */
	public BusinessException(int code, String template, Object... args) {
		super(template);
		this.code = code;
		this.args = args;
	}
}