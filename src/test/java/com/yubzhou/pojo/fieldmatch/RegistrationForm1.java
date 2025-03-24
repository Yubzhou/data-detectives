package com.yubzhou.pojo.fieldmatch;

import com.yubzhou.validator.FieldMatch;
import lombok.Data;

/**
 * 简单字段校验
 * 用户注册表单
 * <p>
 * 校验规则：邮箱和确认邮箱必须一致
 */
@Data
@FieldMatch(
		field1 = "email",
		field2 = "confirmEmail",
		fieldType = String.class
		// message = "邮箱地址不匹配"
)
public class RegistrationForm1 {
	private String email;
	private String confirmEmail;
}