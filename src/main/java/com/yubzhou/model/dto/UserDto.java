package com.yubzhou.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
	@Pattern(regexp = "^[a-zA-Z0-9]{3,20}$", message = "用户名的长度必须在3到20之间，且只能包含字母和数字")
	private String username;
	@NotBlank(message = "密码不能为空")
	private String password;
}
