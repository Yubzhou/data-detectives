package com.yubzhou.interceptor;

import com.yubzhou.annotation.JwtIgnore;
import com.yubzhou.common.UserToken;
import com.yubzhou.util.JwtUtil;
import com.yubzhou.util.LocalAssert;
import com.yubzhou.util.WebContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * JWT鉴权拦截器
 */
@Slf4j
public class JwtAuthInterceptor implements HandlerInterceptor {
	private final JwtUtil jwtUtil;

	public JwtAuthInterceptor(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	// 方法返回true表示继续执行后续的拦截器，返回false表示中断执行，不再执行后续的拦截器
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 从指定的http请求头中取出accessToken
		final String accessToken = request.getHeader(jwtUtil.getJwtProperties().getTokenHeader());
		// 如果不是映射到方法，直接通过
		// 用来判断当前请求是否对应于一个具体的控制器方法，如果不是，则意味着该请求可能是针对静态资源或其他非控制器方法类型的请求，因此不需要进行额外的处理（比如身份验证）
		log.info("jwt拦截器：请求的控制器方法：{}, 请求方法：{}，请求路径：{}", handler.getClass().getName(), request.getMethod(),
				request.getServletPath());
		// 强制放行所有 OPTIONS 请求
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			// response.setHeader("Access-Control-Expose-Headers", TokenInvalidException.TOKEN_INVALID_HEADER);
			response.setStatus(HttpServletResponse.SC_OK); // 明确返回 200
			log.info("OPTIONS 请求已放行，路径：{}", request.getServletPath());
			return true; // 中断后续处理
		}
		if (!(handler instanceof HandlerMethod)) {
			// 输出请求方法和请求路径
			return true;
		}
		// 如果该方法上存在JwtIgnore注解且值为true（或者该类上存在JwtIgnore注解且值为true），则直接通过
		if (isJwtIgnore((HandlerMethod) handler)) {
			return true;
		}
		// 要检查的字符串，如果为null、长度为0或只包含空白字符则抛出自定义异常 BusinessException
		LocalAssert.assertTokenHasText(accessToken, "accessToken不能为空");
		// 内部会验证token是否有效（无效自动抛出异常），并获取token内部信息
		UserToken userToken = jwtUtil.getUserToken(accessToken); // 获取到token内部信息
		// 将用户相关的自定义声明放入本地缓存
		WebContextUtil.setUserToken(userToken);
		// 将accessToken存入本地缓存
		WebContextUtil.setAccessToken(accessToken);
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		// 请求结束后，移除缓存的token
		WebContextUtil.removeContext();
	}

	/**
	 * 判断该方法是否存在JwtIgnore注解且值为true
	 *
	 * @param handlerMethod 方法
	 * @return true：存在JwtIgnore注解且值为true，false：不存在或注解值为false
	 */
	private boolean isJwtIgnore(HandlerMethod handlerMethod) {
		// 先判断该方法是否存在JwtIgnore注解
		Method method = handlerMethod.getMethod();
		if (method.isAnnotationPresent(JwtIgnore.class)) {
			JwtIgnore jwtIgnore = method.getAnnotation(JwtIgnore.class);
			if (jwtIgnore.value()) {
				return true;
			}
		}
		// 如果该方法不存在JwtIgnore注解，则判断该类是否存在JwtIgnore注解
		Class<?> clazz = handlerMethod.getBeanType();
		if (clazz.isAnnotationPresent(JwtIgnore.class)) {
			JwtIgnore jwtIgnore = clazz.getAnnotation(JwtIgnore.class);
			return jwtIgnore.value();
		}
		return false;
	}
}