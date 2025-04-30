package com.yubzhou.model.dto;


import com.yubzhou.common.RegexpConstant;
import com.yubzhou.model.po.User;
import com.yubzhou.validator.FieldMatch;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldMatch(field1 = "password", field2 = "confirmPassword", fieldType = String.class) // 验证两次密码是否一致
public class RegisterUserDto {
	@Pattern(regexp = RegexpConstant.PHONE,
			message = RegexpConstant.PHONE_MESSAGE)
	private String phone;

	@Pattern(regexp = RegexpConstant.PASSWORD,
			message = RegexpConstant.PASSWORD_MESSAGE)
	private String password;

	private String confirmPassword;

	// 短信验证码
	@Pattern(regexp = RegexpConstant.CAPTCHA,
			message = RegexpConstant.CAPTCHA_MESSAGE)
	private String captcha;

	public static User toEntity(RegisterUserDto registerUserDto) {
		User user = new User();
		user.setPhone(registerUserDto.getPhone());
		user.setPassword(registerUserDto.getPassword());
		return user;
	}
}
