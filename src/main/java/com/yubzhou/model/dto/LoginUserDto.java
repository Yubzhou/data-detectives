package com.yubzhou.model.dto;

import com.yubzhou.common.RegexpConstant;
import com.yubzhou.model.po.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserDto {
	@Pattern(regexp = RegexpConstant.PHONE,
			message = RegexpConstant.PHONE_MESSAGE)
	private String phone;

	// 短信验证码
	@Pattern(regexp = RegexpConstant.CAPTCHA,
			message = RegexpConstant.CAPTCHA_MESSAGE,
			groups = {CaptchaLogin.class})
	private String captcha;

	@NotBlank(message = "密码不能为空",
			groups = {PasswordLogin.class})
	private String password;


	public static User toEntity(LoginUserDto loginUserDto) {
		User user = new User();
		user.setPhone(loginUserDto.getPhone());
		user.setPassword(loginUserDto.getPassword());
		return user;
	}


	public interface CaptchaLogin {
	}

	public interface PasswordLogin {
	}
}
