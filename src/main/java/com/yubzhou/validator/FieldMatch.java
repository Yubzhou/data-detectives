package com.yubzhou.validator;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 校验两个字段（或嵌套路径）的值是否一致的通用注解
 * <p>
 * 使用场景：
 * - 密码/确认密码校验
 * - 邮箱/确认邮箱校验
 * - 复杂对象字段的一致性校验（如 user1 与 user2），User 类正确重写 equals() 和 hashCode() 方法
 * - 复杂对象中嵌套字段的一致性校验（如 user.profile.email 与 user.confirmProfile.email）
 * <p>
 * 示例：
 * {@code @FieldMatch(field1 = "email", field2 = "confirmEmail", message = "邮箱不匹配")}
 */
@Target({ElementType.TYPE})       // 作用于类或接口
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
@Constraint(validatedBy = FieldMatchValidator.class) // 关联校验器
@Repeatable(FieldMatch.List.class) // 支持重复注解
@Documented
public @interface FieldMatch {
	/**
	 * 错误提示消息（支持 {field1} 和 {field2} 占位符）
	 * 默认消息示例："邮箱地址 必须与 确认邮箱 一致"
	 */
	String message() default "{field2} 必须与 {field1} 一致";

	/**
	 * 校验分组（用于按场景分组校验）
	 */
	Class<?>[] groups() default {};

	/**
	 * 负载信息（通常用于自定义元数据）
	 */
	Class<? extends Payload>[] payload() default {};

	/**
	 * 第一个字段路径（支持嵌套路径，如 "user.email"）
	 */
	String field1();

	/**
	 * 第二个字段路径（需与 field1 比较的字段）
	 */
	String field2();

	/**
	 * 字段类型（可选，用于强制校验字段类型一致性）
	 * 示例：fieldType = String.class 确保两个字段均为字符串
	 */
	Class<?> fieldType() default Object.class;

	/**
	 * 字段类型错误提示消息（支持 {type} 占位符）
	 * 默认消息示例："字段类型必须为 String"
	 */
	String typeMessage() default "字段类型必须为 {type} 类型";

	/**
	 * 容器注解，用于支持 {@link Repeatable} 重复注解
	 */
	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		FieldMatch[] value(); // 存储多个 @FieldMatch 注解
	}
}