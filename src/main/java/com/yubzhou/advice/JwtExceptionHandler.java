package com.yubzhou.advice;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.util.WebContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(3)
@RestControllerAdvice
@Slf4j
public class JwtExceptionHandler {
	@ExceptionHandler(JWTVerificationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
	public Result<Void> handleJWTVerificationException(JWTVerificationException e, HttpServletRequest request) {
		if (e instanceof JWTDecodeException) {
			log.error("JWTDecodeException: {}", e.getMessage());
			return Result.fail(ReturnCode.INVALID_TOKEN);
		} else if (e instanceof TokenExpiredException) {
			String expiredOn = formatDateTime(request, ((TokenExpiredException) e).getExpiredOn());
			log.error("TokenExpiredException: {}, expired at {}", e.getMessage(), expiredOn);
			return Result.fail(ReturnCode.EXPIRED_TOKEN.getCode(), "token已过期，失效时间：" + expiredOn);
		}
		log.error("JWTVerificationException: {}", e.getMessage());
		return Result.fail(ReturnCode.INVALID_TOKEN);
	}

	private String formatDateTime(HttpServletRequest request, Instant expiredOn) {
		ZoneId zoneId = WebContextUtil.getTimeZone();
		if (zoneId == null) zoneId = ZoneId.of("UTC");
		ZonedDateTime zonedDateTime = expiredOn.atZone(zoneId);
		// return zonedDateTime.toString();
		return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
}
