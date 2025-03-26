package com.yubzhou.model.dto;

import com.yubzhou.common.RegexpConstant;
import com.yubzhou.validator.FieldMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldMatch(field1 = "newPassword", field2 = "confirmPassword", fieldType = String.class) // 验证两次密码是否一致
public class UpdateUserPasswordDto {
	@NotBlank(message = "原密码不能为空")
	private String oldPassword; // 原密码

	@Pattern(regexp = RegexpConstant.PASSWORD,
			message = RegexpConstant.PASSWORD_MESSAGE)
	private String newPassword; // 新密码

	private String confirmPassword; // 确认密码
}
