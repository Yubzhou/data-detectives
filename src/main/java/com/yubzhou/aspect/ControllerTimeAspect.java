package com.yubzhou.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerTimeAspect {

	private static final Logger logger = LoggerFactory.getLogger(ControllerTimeAspect.class);

	// 定义切点：拦截所有Controller类的public方法
	// 匹配 com.yubzhou.controller 包及其子包下所有类的 public 方法
	@Pointcut("execution(public * com.yubzhou.controller..*.*(..))")
	public void controllerPointcut() {
	}

	@Around("controllerPointcut()")
	public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();
		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getSimpleName();

		try {
			Object result = joinPoint.proceed(); // 执行目标方法
			long endTime = System.currentTimeMillis();
			logger.info("[{}::{}] 执行结束，耗时：{}ms", className, methodName, endTime - startTime);
			return result;
		} catch (Throwable e) {
			long endTime = System.currentTimeMillis();
			logger.error("[{}::{}] 执行异常，耗时：{}ms，异常信息：{}", className, methodName, endTime - startTime, e.getMessage());
			throw e; // 重新抛出异常，确保接口能正常返回错误信息
		}
	}
}