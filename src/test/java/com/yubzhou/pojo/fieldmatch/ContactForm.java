package com.yubzhou.pojo.fieldmatch;

import com.yubzhou.validator.FieldMatch;
import lombok.Data;

/**
 * 多组校验
 * 联系方式表单
 * <p>
 * 校验规则：
 * 1. 邮箱与确认邮箱一致
 * 2. 手机号与确认手机号一致
 */

@Data
@FieldMatch.List({
		@FieldMatch(field1 = "email", field2 = "confirmEmail", fieldType = String.class),
		@FieldMatch(field1 = "phone", field2 = "confirmPhone", fieldType = String.class)
})
public class ContactForm {
	private String email;
	private String confirmEmail;
	private String phone;
	private String confirmPhone;
}