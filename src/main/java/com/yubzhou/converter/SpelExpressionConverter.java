package com.yubzhou.converter;

import com.yubzhou.util.SpelEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SpelExpressionConverter implements Converter<String, Integer> {

	private final SpelEvaluator spelEvaluator;

	@Autowired
	public SpelExpressionConverter(SpelEvaluator spelEvaluator) {
		this.spelEvaluator = spelEvaluator;
	}

	@Override
	public Integer convert(String source) {
		if (spelEvaluator.isSpelExpression(source)) {
			try {
				return spelEvaluator.evaluate(source, Integer.class);
			} catch (Exception ex) {
				throw new SpelConversionException(
						"SpEL evaluation failed for expression: " + source, ex);
			}
		}
		return Integer.parseInt(source);
	}
}

// 自定义异常类
class SpelConversionException extends RuntimeException {
	public SpelConversionException(String message, Throwable cause) {
		super(message, cause);
	}
}