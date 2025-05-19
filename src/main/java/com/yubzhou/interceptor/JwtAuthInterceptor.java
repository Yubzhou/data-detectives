package com.yubzhou.interceptor;

import com.yubzhou.annotation.JwtIgnore;
import com.yubzhou.common.UserToken;
import com.yubzhou.util.ClientFingerprintUtil;
import com.yubzhou.util.JwtUtil;
import com.yubzhou.util.LocalAssert;
import com.yubzhou.util.WebContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
		HandlerMethod handlerMethod = handler instanceof HandlerMethod ? (HandlerMethod) handler : null;
		log.info("jwt拦截器：客户端IP：{}，请求的控制器方法：{}, 请求方法：{}，请求路径：{}",
				ClientFingerprintUtil.getClientIp(request),
				handlerMethod != null ? handlerMethod.getMethod() : "非Controller方法",
				request.getMethod(),
				request.getServletPath());
		// 强制放行所有 OPTIONS 请求
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK); // 明确返回 200
			// log.info("OPTIONS 请求已放行，路径：{}", request.getServletPath());
			return true;
		}
		// 如果不是映射到方法（Controller），直接通过
		if (handlerMethod == null) {
			return true;
		}
		// 如果该方法需要跳过JWT校验，直接通过
		if (shouldSkipJwtValidation(handlerMethod)) {
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

	// /**
	//  * JWT校验忽略判断方法
	//  */
	// private boolean shouldSkipJwtValidation(HandlerMethod handlerMethod) {
	// 	// 获取方法层注解（优先判断）
	// 	Method method = handlerMethod.getMethod();
	// 	JwtIgnore methodAnnotation = method.getAnnotation(JwtIgnore.class);
	//
	// 	// 方法层明确声明时立即返回
	// 	if (methodAnnotation != null) {
	// 		return methodAnnotation.value();
	// 	}
	//
	// 	// 获取类层注解（次级判断）
	// 	Class<?> clazz = handlerMethod.getBeanType();
	// 	JwtIgnore classAnnotation = clazz.getAnnotation(JwtIgnore.class);
	//
	// 	return classAnnotation != null && classAnnotation.value();
	// }

	// 使用 ConcurrentHashMap 实现注解缓存
	private final Map<Method, Boolean> methodCache = new ConcurrentHashMap<>(256);
	private final Map<Class<?>, Boolean> classCache = new ConcurrentHashMap<>(256);

	/**
	 * 判断是否需要跳过JWT校验（带缓存优化）
	 * 方法上JwtIgnore注解的优先级高于类级
	 * 方法级缓存：使用 ConcurrentHashMap 实现注解缓存，以优化方法级注解的判断
	 * 类级缓存：使用 ConcurrentHashMap 实现注解缓存，以优化类级注解的判断
	 */
	private boolean shouldSkipJwtValidation(HandlerMethod handlerMethod) {
		// log.info("methodCache：{}", methodCache);
		// log.info("classCache：{}", classCache);

		Method method = handlerMethod.getMethod();

		// 优先检查方法级缓存
		return methodCache.computeIfAbsent(method, m -> {
			// 方法级注解判断
			JwtIgnore methodAnnotation = m.getAnnotation(JwtIgnore.class);
			if (methodAnnotation != null) {
				return methodAnnotation.value();
			}

			// 未找到方法级注解时，检查类级缓存
			// 使用 AopUtils.getTargetClass 将始终获取到 获取原始目标类，而不会返回代理类
			Class<?> targetClass = AopUtils.getTargetClass(handlerMethod.getBean());
			return classCache.computeIfAbsent(targetClass, clazz -> {
				JwtIgnore classAnnotation = clazz.getAnnotation(JwtIgnore.class);
				return classAnnotation != null && classAnnotation.value();
			});
		});
	}
}