package com.yubzhou.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yubzhou.model.po.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVo {
	private Long userId; // 用户id

	private String phone; // 手机号

	private String nickname; // 昵称

	private Short gender; // 性别（0:未知，1:男，2:女）

	private String avatarUrl; // 头像url

	private Set<String> interestedFields; // 兴趣领域

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime registerTime; // 注册时间

	public static UserProfileVo fromUserProfile(UserProfile profile, String phone, LocalDateTime registerTime) {
		UserProfileVo vo = new UserProfileVo();
		vo.setUserId(profile.getUserId());
		vo.setPhone(phone);
		vo.setGender(profile.getGender());
		vo.setAvatarUrl(profile.getAvatarUrl());
		vo.setInterestedFields(profile.getInterestedFields());
		vo.setRegisterTime(registerTime);
		return vo;
	}
}
