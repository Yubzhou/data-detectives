package com.yubzhou.interceptor;

import com.yubzhou.util.WebContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.DateTimeException;
import java.time.ZoneId;

@Slf4j
public class TimeZoneInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String timeZone = request.getHeader(WebContextUtil.TIME_ZONE_HEADER);
		ZoneId zoneId = ZoneId.of("UTC"); // 默认时区为 UTC
		if (StringUtils.hasText(timeZone)) {
			try {
				// 验证时区有效性
				zoneId = ZoneId.of(timeZone);
			} catch (DateTimeException e) {
				// // 时区无效，返回400错误
				// response.setStatus(HttpStatus.BAD_REQUEST.value());
				// throw new IllegalArgumentException("时区格式错误: " + timeZone);
				log.warn("TimeZoneInterceptor拦截器中警告：【时区格式错误：{}】", timeZone);
			}
		}
		WebContextUtil.setTimeZone(zoneId); // 设置时区
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		WebContextUtil.removeContext();
	}
}