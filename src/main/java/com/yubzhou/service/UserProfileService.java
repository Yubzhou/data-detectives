package com.yubzhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.dto.UpdateUserPasswordDto;
import com.yubzhou.model.po.UserProfile;

import java.util.Set;

public interface UserProfileService extends IService<UserProfile>  {

	void insertUserProfile(Long userId);

	UserProfile getProfileByUserId();

	void updateInterests(Set<String> interests);

	void updateUserProfile(UserProfile userProfile);

	void updateAvatarUrl(String avatarUrl);

	void updateNickname(String nickname);

	void updatePhone(String phone, String captcha);

	void updatePassword(UpdateUserPasswordDto dto);
}
