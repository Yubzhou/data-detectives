package com.yubzhou.advice;

import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * 数据校验异常处理器
 */
@Order(1)
@RestControllerAdvice
@Slf4j
public class ValidationExceptionHandler {
	/**
	 * 处理BindException异常
	 * 触发场景：请求参数绑定到 @ModelAttribute 对象失败（如类型转换错误、字段校验失败）
	 * 适用场景：表单提交、GET 请求参数校验
	 *
	 * @param e BindException
	 * @return Result<Void>
	 */
	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Map<String, List<Result.FieldError>>> handleBindException(BindException e) {
		log.error("BindException: {}", e.getMessage());
		// List<String> errorMessages = e.getBindingResult().getFieldErrors()
		// 		.stream()
		// 		.map(FieldError::getDefaultMessage)
		// 		.collect(Collectors.toList());
		// String message = String.join("；", errorMessages);
		// return Result.fail(ReturnCode.RC400.getCode(), message);

		List<Result.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
				.map(error -> new Result.FieldError(
						error.getField(),
						error.getDefaultMessage()))
				.toList();
		return Result.failOnFieldError(ReturnCode.RC400, errors);
	}

	/**
	 * 处理MethodArgumentNotValidException异常
	 * 触发场景：请求体（如 JSON）绑定到 @RequestBody 对象失败，校验失败时触发
	 * 适用场景：JSON 请求参数校验
	 *
	 * @param e MethodArgumentNotValidException
	 * @return Result<Void>
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Map<String, List<Result.FieldError>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.error("MethodArgumentNotValidException: {}", e.getMessage());
		// List<String> errorMessages = e.getBindingResult().getFieldErrors()
		// 		.stream()
		// 		.map(FieldError::getDefaultMessage)
		// 		.collect(Collectors.toList());
		// String message = String.join("；", errorMessages);
		// return Result.fail(ReturnCode.RC400.getCode(), message);

		List<Result.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
				.map(error -> new Result.FieldError(
						error.getField(),
						error.getDefaultMessage()))
				.toList();
		return Result.failOnFieldError(ReturnCode.RC400, errors);
	}

	/**
	 * 处理ConstraintViolationException异常
	 * 触发场景：方法参数或返回值校验失败（如 @RequestParam、@PathVariable 校验失败）
	 * 适用场景：方法参数校验
	 *
	 * @param e ConstraintViolationException
	 * @return Result<Void>
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Map<String, List<Result.FieldError>>> handleConstraintViolationException(ConstraintViolationException e) {
		log.error("ConstraintViolationException: {}", e.getMessage());
		// Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
		// List<String> errorMessages = violations.stream()
		// 		.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
		// 		.collect(Collectors.toList());
		// String message = String.join("；", errorMessages);
		// return Result.fail(ReturnCode.RC400.getCode(), message);

		List<Result.FieldError> errors = e.getConstraintViolations().stream()
				.map(violation -> {
					String field = violation.getPropertyPath().toString();
					// 提取方法参数名（如 "createUser.name" -> "name"）
					if (field.contains(".")) {
						field = field.substring(field.lastIndexOf(".") + 1);
					}
					return new Result.FieldError(
							field,
							violation.getMessage());
				})
				.toList();
		return Result.failOnFieldError(ReturnCode.RC400, errors);
	}
}