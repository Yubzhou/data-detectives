package com.yubzhou.pojo.fieldmatch;

import com.yubzhou.validator.FieldMatch;
import lombok.Data;

/**
 * 复杂对象嵌套路径校验
 * 用户信息更新请求
 * <p>
 * 校验规则：用户资料中的邮箱与确认邮箱必须一致
 */
@Data
@FieldMatch(
		field1 = "user.profile.email",
		field2 = "user.confirmProfile.email",
		fieldType = String.class // 强制字段类型为字符串
)
public class UserUpdateRequest {
	private User user; // 嵌套复杂对象
}