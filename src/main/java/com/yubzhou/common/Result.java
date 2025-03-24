package com.yubzhou.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class Result<T> {
	private int code;
	private String message;
	private T data;
	private Instant timestamp;

	@Data
	@AllArgsConstructor
	public static class FieldError {
		private String field;    // 字段名
		private String message;  // 错误消息

		public static Map<String, List<FieldError>> result(List<FieldError> errors) {
			return Map.of("errors", errors);
		}
	}

	public Result() {
		this.timestamp = Instant.now();
	}

	public Result(ReturnCode rc, T data) {
		this(); // 调用无参构造器初始化timestamp
		this.code = rc.getCode();
		this.message = rc.getMessage();
		this.data = data;
	}

	public Result(int code, String message, T data) {
		this(); // 调用无参构造器初始化timestamp
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public static <T> Result<T> of(int code, String message, T data) {
		return new Result<>(code, message, data);
	}

	// 默认响应码为 200
	public static Result<Void> success() {
		return success(null);
	}

	// 默认响应码为 200
	public static <T> Result<T> success(T data) {
		return new Result<>(ReturnCode.RC200, data);
	}

	// 默认响应码为 200
	public static <T> Result<T> success(String message, T data) {
		return new Result<>(ReturnCode.RC200.getCode(), message, data);
	}

	// 默认响应码为 200
	public static Result<Void> successWithMessage(String message) {
		return success(message, null);
	}

	// 默认响应码为 500
	public static Result<Void> fail(String message) {
		return new Result<>(ReturnCode.RC500.getCode(), message, null);
	}

	public static Result<Void> fail(ReturnCode rc) {
		return new Result<>(rc, null);
	}

	public static <T> Result<T> fail(ReturnCode rc, T data) {
		return new Result<>(rc, data);
	}

	public static Result<Void> fail(int code, String message) {
		return new Result<>(code, message, null);
	}

	public static <T> Result<T> fail(int code, String message, T data) {
		return new Result<>(code, message, data);
	}

	public static Result<Map<String, List<FieldError>>> failOnFieldError(ReturnCode rc, List<FieldError> errors) {
		return new Result<>(rc, FieldError.result(errors));
	}
}
