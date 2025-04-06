package com.yubzhou.model.vo;

import com.yubzhou.model.po.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

	private long registerDays; // 注册天数

	public static UserProfileVo fromUserProfile(UserProfile profile, String phone, LocalDateTime registerTime) {
		UserProfileVo vo = new UserProfileVo();
		vo.setUserId(profile.getUserId());
		// 数据脱敏，将手机号脱敏
		vo.setPhone(getPhoneMasked(phone));
		vo.setNickname(profile.getNickname());
		vo.setGender(profile.getGender());
		vo.setAvatarUrl(profile.getAvatarUrl());
		vo.setInterestedFields(profile.getInterestedFields());
		// 将注册时间转为注册天数
		vo.setRegisterDays(getRegisterDays(registerTime));
		return vo;
	}

	// 数据脱敏，将手机号脱敏
	private static String getPhoneMasked(String phone) {
		// 将手机号的第4-8位进行隐藏
		return phone.substring(0, 3) + "*****" + phone.substring(8);
	}

	// 将注册时间转为注册天数
	private static long getRegisterDays(LocalDateTime registerTime) {
		return ChronoUnit.DAYS.between(registerTime, LocalDateTime.now());
	}
}
