package com.yubzhou.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpelEvaluator {

	// 注入需要的BeanFactory等资源
	private final BeanFactory beanFactory;

	@Autowired
	public SpelEvaluator(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	// 缓存配置（最大1000条目，LRU策略）
	private static final int MAX_CACHE_SIZE = 1000;
	private final Map<String, Expression> expressionCache =
			new ConcurrentHashMap<>();

	private ExpressionParser parser;
	private StandardEvaluationContext evaluationContext;

	// 初始化配置
	@PostConstruct
	public void init() {
		// 启用SpEL编译加速
		SpelParserConfiguration config = new SpelParserConfiguration(
				SpelCompilerMode.IMMEDIATE, getClass().getClassLoader());

		this.parser = new SpelExpressionParser(config);
		this.evaluationContext = new StandardEvaluationContext();
		evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
	}

	/**
	 * 安全解析并缓存表达式
	 *
	 * @param expression SpEL表达式（无需包含#{ }）
	 * @return 解析后的Expression对象
	 */
	public Expression parseExpression(String expression) {
		return expressionCache.computeIfAbsent(expression, expr -> {
			if (expressionCache.size() >= MAX_CACHE_SIZE) {
				expressionCache.clear(); // 简单清除策略，生产环境建议LRU
			}
			return parser.parseExpression(expr);
		});
	}

	/**
	 * 执行表达式求值
	 *
	 * @param expression 完整表达式（包含#{ }）
	 * @param targetType 返回类型
	 * @return 求值结果
	 */
	public <T> T evaluate(String expression, Class<T> targetType) {
		String exprBody = extractExpressionBody(expression);
		Expression expr = parseExpression(exprBody);
		return expr.getValue(evaluationContext, targetType);
	}

	// 清理缓存（可用于热更新）
	public void clearCache() {
		expressionCache.clear();
	}

	// 获取缓存统计信息
	public Map<String, Object> getCacheStats() {
		return Map.of(
				"cacheSize", expressionCache.size(),
				"maxSize", MAX_CACHE_SIZE
		);
	}

	private String extractExpressionBody(String expression) {
		if (isSpelExpression(expression)) {
			return expression.substring(2, expression.length() - 1);
		}
		return expression;
	}

	public boolean isSpelExpression(String expression) {
		return expression != null && expression.startsWith("#{") && expression.endsWith("}");
	}
}