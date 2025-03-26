package com.yubzhou.advice;

import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.util.MessageFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.concurrent.CompletionException;

/**
 * 全局异常处理器
 */
// 默认值为最低优先级，可以不用设置（其值越小，优先级越高）
@Order() // 优先级最低，保证其他异常处理类先加载，防止被通用的Exception异常拦截
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	// 处理请求路径不存在的异常
	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
		log.error("NoHandlerFoundException: {}", e.getMessage());
		return Result.fail(ReturnCode.RC404.getCode(), "请求的路径不存在: " + e.getRequestURL());
	}

	// 处理请求资源不存在的异常
	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
		log.error("NoResourceFoundException: {}", e.getMessage());
		if (e.getResourcePath().endsWith("/favicon.ico")) {
			// 忽略 favicon.ico 的误报日志
			return Result.fail(ReturnCode.RC404.getCode(), "网站图标 favicon.ico 未配置");
		}
		return Result.fail(ReturnCode.RC404.getCode(), "请求的资源不存在: " + e.getResourcePath());
	}

	// 处理请求方法不支持的异常
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
		log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());
		return Result.fail(ReturnCode.RC405.getCode(), "不支持的请求方法: " + e.getMethod());
	}

	// 处理参数不合法的异常
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
		log.error("IllegalArgumentException: {}", e.getMessage());
		return Result.fail(ReturnCode.RC400.getCode(), "参数不合法: " + e.getMessage());
	}

	// 处理 CompletionException 异常：CompletionException 异常是由 CompletableFuture 类抛出的，
	@ExceptionHandler(CompletionException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Result<Void> handleCompletionException(CompletionException e) {
		log.error("异步任务执行异常 CompletionException: {}", e.getMessage());
		return Result.fail(ReturnCode.RC500.getCode(), "服务器内部错误: " + e.getMessage());
	}

	// 处理自定义业务异常
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
		String formattedMessage = MessageFormatUtil.formatMessage(e.getMessage(), e.getArgs());
		log.error("BusinessException: {}", formattedMessage);
		// 使用 ResponseEntity 返回自定义状态码
		return ResponseEntity.status(e.getCode()).body(Result.fail(e.getCode(), formattedMessage));
	}

	// 处理其他异常
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Result<Void> handleException(Exception e) {
		log.error("Exception Name: {}, Message: {}", e.getClass().getName(), e.getMessage());
		log.error("Exception StackTrace: ", e);
		return Result.fail(ReturnCode.RC500.getCode(), "服务器内部错误: " + e.getMessage());
	}
}