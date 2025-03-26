package com.yubzhou.advice;

import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Order(2)
@RestControllerAdvice
@Slf4j
public class SqlExceptionHandler {
	/**
	 * 处理DuplicateKeyException异常
	 * 触发场景：当插入数据时，唯一索引冲突，导致插入失败
	 * 适用场景：一般用于数据插入操作
	 *
	 * @param e DuplicateKeyException
	 * @return Result<Void>
	 */
	// @ExceptionHandler(DuplicateKeyException.class)
	// @ResponseStatus(HttpStatus.CONFLICT)
	// public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
	// 	log.error("DuplicateKeyException: {}", e.getMessage());
	// 	String[] messages = e.getMessage().split(";");
	// 	// 如果messages数组长度大于1，则取第二个元素，否则返回e.getMessage()
	// 	return Result.fail(ReturnCode.RC409.getCode(), "Duplicate Key Exception: " + (messages.length > 1 ? messages[1] : e.getMessage()));
	// }
	@ExceptionHandler(DuplicateKeyException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public Result<Void> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
		log.error("DuplicateKeyException: {}", e.getMessage());
		String[] messages = e.getMessage().split(";");
		// 如果messages数组长度大于1，则取第二个元素，否则返回e.getMessage()
		String message = messages.length > 1 ? messages[1] : e.getMessage();
		if (message.contains("users.uniq_phone")) {
			message = "注册失败：该手机号码已被注册";
			if (request.getRequestURI().contains("/api/profile/phone")) message = "修改手机号码失败：该手机号码已被绑定";
		} else if (message.contains("user_profiles.uniq_user")) {
			message = "更新个人信息失败：该用户已存在";
		}
		return Result.fail(HttpStatus.CONFLICT.value(), message);
	}
}