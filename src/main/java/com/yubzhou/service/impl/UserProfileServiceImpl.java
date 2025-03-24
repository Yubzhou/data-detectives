package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.mapper.UserProfileMapper;
import com.yubzhou.model.po.UserProfile;
import com.yubzhou.service.UserProfileService;
import com.yubzhou.util.WebContextUtil;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {
	@Override
	public UserProfile getProfileByUserId() {
		Long userId = WebContextUtil.getUserToken().getUserId();
		return this.lambdaQuery()
				.select(UserProfile::getId, UserProfile::getUserId, UserProfile::getNickName, UserProfile::getGender, UserProfile::getAvatarUrl, UserProfile::getInterestedFields)
				.eq(UserProfile::getUserId, userId)
				.last("LIMIT 1")
				.one();
	}

	@Override
	public void updateInterests(Set<String> interests) {
		// 创建实体对象并设置字段值
		UserProfile profile = new UserProfile();
		profile.setInterestedFields(interests);

		// 执行更新（通过实体对象传值）
		// 重点：如果想触发自定义的 TypeHandler，必须通过实体对象传值，而不是通过 wrapper.set() 方法
		this.updateUserProfile(profile);
	}

	@Override
	public void updateUserProfile(UserProfile userProfile) {
		this.updateUserProfile(userProfile, null);
	}


	private void updateUserProfile(UserProfile userProfile, LambdaUpdateWrapper<UserProfile> wrapper) {
		// 1. 获取当前用户 ID
		Long userId = WebContextUtil.getUserToken().getUserId();

		// 2. 设置条件
		if (wrapper == null) wrapper = new LambdaUpdateWrapper<>();
		wrapper.eq(UserProfile::getUserId, userId);

		// 3. 执行更新（通过实体对象传值）
		// 重点：如果想触发自定义的 TypeHandler，必须通过实体对象传值，而不是通过 wrapper.set() 方法
		this.update(userProfile, wrapper);
	}
}
