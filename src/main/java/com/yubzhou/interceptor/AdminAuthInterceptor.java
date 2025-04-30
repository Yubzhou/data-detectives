package com.yubzhou.interceptor;

import com.yubzhou.common.ReturnCode;
import com.yubzhou.common.UserToken;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.util.WebContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 获取当前用户令牌（如果不存在则自动抛出异常）
		UserToken userToken = WebContextUtil.getUserToken();

		// 检查管理员权限（拥有Admin或SuperAdmin权限）
		if (!userToken.getRole().isAdmin()) {
			throw new BusinessException(ReturnCode.ACCESS_DENIED);
		}

		return true;
	}
}