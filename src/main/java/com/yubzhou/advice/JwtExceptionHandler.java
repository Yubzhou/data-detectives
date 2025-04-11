package com.yubzhou.advice;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.exception.TokenInvalidException;
import com.yubzhou.util.WebContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Order(0)
@RestControllerAdvice
@Slf4j
public class JwtExceptionHandler {
	// JWT验证异常，返回401状态码，并返回自定义状态码和消息
	@ExceptionHandler(JWTVerificationException.class)
	public ResponseEntity<Result<Void>> handleJWTVerificationException(JWTVerificationException e,
																	   HttpServletRequest request) {
		Result<Void> result = Result.fail(ReturnCode.INVALID_TOKEN);
		if (e instanceof JWTDecodeException) {
			log.error("JWTDecodeException: {}", e.getMessage());
			result = Result.fail(ReturnCode.INVALID_TOKEN);
		} else if (e instanceof TokenExpiredException) {
			String expiredOn = formatDateTime(request, ((TokenExpiredException) e).getExpiredOn());
			log.error("TokenExpiredException: {}, expired at {}", e.getMessage(), expiredOn);
			result = Result.fail(ReturnCode.EXPIRED_TOKEN.getCode(), "token已过期，失效时间：" + expiredOn);
		}
		log.error("JWTVerificationException: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401
				.header(TokenInvalidException.TOKEN_INVALID_HEADER, "true") // 添加自定义token无效header
				.body(result);
	}

	// 自定义token无效异常，返回401状态码，并返回自定义状态码和消息
	@ExceptionHandler(TokenInvalidException.class)
	public ResponseEntity<Result<Void>> handleTokenInvalidException(TokenInvalidException e) {
		log.error("TokenInvalidException: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401
				.header(TokenInvalidException.TOKEN_INVALID_HEADER, "true") // 添加自定义token无效header
				.body(Result.fail(e.getCode(), e.getMessage()));
	}

	private String formatDateTime(HttpServletRequest request, Instant expiredOn) {
		ZoneId zoneId = WebContextUtil.getTimeZone();
		if (zoneId == null) zoneId = ZoneId.of("UTC");
		ZonedDateTime zonedDateTime = expiredOn.atZone(zoneId);
		// return zonedDateTime.toString();
		return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
}
