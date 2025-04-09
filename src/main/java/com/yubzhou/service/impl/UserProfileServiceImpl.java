package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.mapper.UserProfileMapper;
import com.yubzhou.model.dto.UpdateUserPasswordDto;
import com.yubzhou.model.po.User;
import com.yubzhou.model.po.UserProfile;
import com.yubzhou.model.vo.UserProfileVo;
import com.yubzhou.service.UserProfileService;
import com.yubzhou.service.UserService;
import com.yubzhou.util.WebContextUtil;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

	private final UserService userService;

	@Autowired
	public UserProfileServiceImpl(UserService userService) {
		this.userService = userService;
	}

	// 随机生成一个可重复的昵称（字母+数字）
	// Yubzhou TODO 2025/4/9 22:37; 此处需要优化，随机生成一个可重复的昵称（字母+数字）

	@Override
	public void insertUserProfile(Long userId) {
		// 1. 创建实体对象并设置字段值
		UserProfile profile = new UserProfile();
		profile.setUserId(userId);

		// 2. 执行插入
		this.save(profile);
	}

	@Override
	public UserProfileVo getProfileByUserId() {
		long userId = WebContextUtil.getCurrentUserId();
		UserProfile profile = this.lambdaQuery()
				.select(UserProfile::getId, UserProfile::getUserId, UserProfile::getNickname, UserProfile::getGender,
						UserProfile::getAvatarUrl, UserProfile::getInterestedFields)
				.eq(UserProfile::getUserId, userId)
				.last("LIMIT 1")
				.one();

		// 查询用户信息（手机和注册时间）
		User user = userService.findByUserId(userId);
		// 转为Vo对象并返回
		return UserProfileVo.fromUserProfile(profile, user.getPhone(), user.getCreatedAt());
	}

	@Override
	public List<UserProfile> listUserProfiles(Collection<Long> userIds) {
		return this.lambdaQuery()
				.select(UserProfile::getUserId, UserProfile::getNickname, UserProfile::getAvatarUrl)
				.in(UserProfile::getUserId, userIds)
				.list();
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
		this.updateUserProfileByUserId(userProfile, null);
	}

	@Override
	public void updateAvatarUrl(String avatarUrl) {
		LambdaUpdateWrapper<UserProfile> wrapper = new LambdaUpdateWrapper<>();
		wrapper.set(UserProfile::getAvatarUrl, avatarUrl);
		this.updateUserProfileByUserId(wrapper);
	}

	@Override
	public void updateNickname(String nickname) {
		LambdaUpdateWrapper<UserProfile> wrapper = new LambdaUpdateWrapper<>();
		wrapper.set(UserProfile::getNickname, nickname);
		this.updateUserProfileByUserId(wrapper);
	}

	@Override
	public void updatePhone(String phone, String captcha) {
		long userId = WebContextUtil.getCurrentUserId();
		userService.updatePhone(userId, phone, captcha);
	}

	@Override
	public void updatePassword(UpdateUserPasswordDto dto) {
		// 验证新密码是否与原密码相同
		if (dto.getOldPassword().equals(dto.getNewPassword()))
			throw new BusinessException(ReturnCode.NEW_PASSWORD_EQUAL_OLD_PASSWORD);
		long userId = WebContextUtil.getCurrentUserId();
		// 则更新密码，内部会自动判断旧密码是否正确
		userService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
	}

	private void updateUserProfileByUserId(@NonNull UserProfile userProfile, LambdaUpdateWrapper<UserProfile> wrapper) {
		// 1. 获取当前用户 ID
		Long userId = WebContextUtil.getCurrentUserId();

		// 2. 设置条件
		if (wrapper == null) wrapper = new LambdaUpdateWrapper<>();
		wrapper.eq(UserProfile::getUserId, userId);

		// 3. 执行更新（通过实体对象传值）
		// 重点：如果想触发自定义的 TypeHandler，必须通过实体对象传值，而不是通过 wrapper.set() 方法
		this.update(userProfile, wrapper);
	}

	/*
	 * 重写 updateUserProfileByUserId 方法，通过 wrapper 传值
	 * 注意：wrapper 不能为 null
	 */
	private void updateUserProfileByUserId(@NonNull LambdaUpdateWrapper<UserProfile> wrapper) {
		// 1. 获取当前用户 ID
		long userId = WebContextUtil.getCurrentUserId();

		// 2. 设置条件
		wrapper.eq(UserProfile::getUserId, userId);

		// 3. 执行更新（通过 wrapper 传值）
		this.update(wrapper);
	}
}
