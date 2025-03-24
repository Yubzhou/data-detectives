package com.yubzhou.model.dto;

import com.yubzhou.model.po.UserProfile;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserProfileDto {
	@Length(min = 2, max = 15, message = "昵称长度必须在2-15之间")
	private String nickName; // 昵称

	@NotEmpty(message = "感兴趣领域不能为空")
	private Set<String> interestedFields; // 兴趣领域

	private String avatarUrl; // 头像url


	public UserProfile toEntity(RegisterUserProfileDto dto) {
		UserProfile userProfile = new UserProfile();
		userProfile.setNickName(dto.getNickName());
		userProfile.setAvatarUrl(dto.getAvatarUrl());
		userProfile.setInterestedFields(dto.getInterestedFields());
		return userProfile;
	}
}
