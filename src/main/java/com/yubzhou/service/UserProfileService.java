package com.yubzhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.dto.UpdateUserPasswordDto;
import com.yubzhou.model.po.UserProfile;
import com.yubzhou.model.vo.UserProfileVo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserProfileService extends IService<UserProfile> {

	void insertUserProfile(Long userId);

	UserProfileVo getProfileByUserId();

	List<UserProfile> listUserProfiles(Collection<Long> userIds);

	void updateInterests(Set<String> interests);

	void updateUserProfile(UserProfile userProfile);

	void updateAvatarUrl(String avatarUrl);

	void updateNickname(String nickname);

	void updatePhone(String phone, String captcha);

	void updatePassword(UpdateUserPasswordDto dto);
}
