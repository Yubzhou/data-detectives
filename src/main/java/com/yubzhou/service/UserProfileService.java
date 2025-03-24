package com.yubzhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.po.UserProfile;

import java.util.Set;

public interface UserProfileService extends IService<UserProfile>  {

	UserProfile getProfileByUserId();

	void updateInterests(Set<String> interests);

	void updateUserProfile(UserProfile userProfile);
}
