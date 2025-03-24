package com.yubzhou.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Objects;


/**
 * 实现字段匹配校验逻辑的校验器
 * <p>
 * 功能：
 * 1. 解析字段路径（如 "user.profile.email"）
 * 2. 比较两个字段的值
 * 3. 校验失败时，将错误定位到 field2 字段
 */
public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {

	private String field1Path; // 字段1的路径（如 "email" 或 "user.email"）
	private String field2Path; // 字段2的路径
	private Class<?> fieldType; // 字段类型约束
	private String typeMessageTemplate; // 存储注解中的 typeMessage

	/**
	 * 初始化校验器参数
	 *
	 * @param constraintAnnotation 注解配置信息
	 */
	@Override
	public void initialize(FieldMatch constraintAnnotation) {
		this.field1Path = constraintAnnotation.field1();
		this.field2Path = constraintAnnotation.field2();
		this.fieldType = constraintAnnotation.fieldType();
		this.typeMessageTemplate = constraintAnnotation.typeMessage(); // 初始化消息模板
	}

	/**
	 * 执行校验逻辑
	 *
	 * @param target  被校验的对象（如 RegistrationForm）
	 * @param context 校验上下文（用于自定义错误消息）
	 * @return 是否校验通过
	 */
	@Override
	public boolean isValid(Object target, ConstraintValidatorContext context) {
		// 使用 Spring 的 BeanWrapper 解析嵌套路径
		BeanWrapper wrapper = new BeanWrapperImpl(target);
		Object value1 = wrapper.getPropertyValue(field1Path); // 获取字段1的值
		Object value2 = wrapper.getPropertyValue(field2Path); // 获取字段2的值

		// 1. 类型校验优先于值比较
		// 检查类型一致性（如果配置了 fieldType，fieldType默认值为Object.class）
		if (fieldType != Object.class) {
			if (!isTypeValid(value1, value2, context)) {
				return false; // 类型错误直接阻断后续校验
			}
		}

		// 2. 值一致性校验
		boolean matches = Objects.equals(value1, value2);
		if (!matches) {
			// 校验失败时，将错误信息绑定到 field2 字段
			addValueMismatchError(context);
		}
		return matches;
	}

	/**
	 * 校验字段类型是否符合要求
	 */
	private boolean isTypeValid(Object value1, Object value2,
								ConstraintValidatorContext context) {
		if (fieldType.isInstance(value1) && fieldType.isInstance(value2)) {
			return true;
		}

		// 替换 {type} 占位符
		String finalMessage = typeMessageTemplate
				.replace("{type}", fieldType.getSimpleName());

		// 构建类型错误消息
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(finalMessage)
				.addPropertyNode(field2Path)
				.addConstraintViolation();
		return false;
	}

	/**
	 * 添加值不匹配错误
	 */
	private void addValueMismatchError(ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation(); // 禁用默认错误
		String message = context.getDefaultConstraintMessageTemplate()
				.replace("{field1}", field1Path)
				.replace("{field2}", field2Path);
		context.buildConstraintViolationWithTemplate(message)
				// 将校验错误绑定到特定字段
				.addPropertyNode(field2Path)
				.addConstraintViolation();
	}
}