package com.yubzhou.pojo.fieldmatch;

import com.yubzhou.validator.FieldMatch;
import lombok.Data;

@Data
@FieldMatch(
		field1 = "user1",
		field2 = "user2",
		fieldType = User.class
		// message = "用户信息不一致"
)
public class RegistrationForm2 {
	private User user1;
	private User user2;
}