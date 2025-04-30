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
import com.yubzhou.properties.FileUploadProperties;
import com.yubzhou.service.UserProfileService;
import com.yubzhou.service.UserService;
import com.yubzhou.util.PathUtil;
import com.yubzhou.util.WebContextUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
@Slf4j
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

	private final UserService userService;
	private final FileUploadProperties fileUploadProperties;
	private final ThreadPoolTaskExecutor globalTaskExecutor;

	@Autowired
	public UserProfileServiceImpl(UserService userService,
								  FileUploadProperties fileUploadProperties,
								  @Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor) {
		this.userService = userService;
		this.fileUploadProperties = fileUploadProperties;
		this.globalTaskExecutor = globalTaskExecutor;
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
	public UserProfileVo getProfileByUserId(Long userId) {
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

	private String getAvatarUrlByUserId(long userId) {
		UserProfile profile = this.lambdaQuery()
				.select(UserProfile::getAvatarUrl)
				.eq(UserProfile::getUserId, userId)
				.last("LIMIT 1")
				.one();
		return profile.getAvatarUrl();
	}

	// 从url中提取文件名
	public String getFileNameFromUrl(String url) {
		if (url == null) return null;
		// 以字符串的最后一个 / 为分界
		int lastSlashIndex = url.lastIndexOf("/");
		if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
			return url.substring(lastSlashIndex + 1);
		} else {
			return null;
		}
	}

	// 异步删除旧头像
	private void deleteOldAvatar(Long userId) {
		globalTaskExecutor.execute(() -> {
			// 查询数据库获取用户旧头像
			String avatarUrl = getAvatarUrlByUserId(userId);
			if (!StringUtils.hasText(avatarUrl)) {
				log.info("用户旧头像为空，无需删除");
				return;
			}
			if (avatarUrl.startsWith("/default/avatar/")) {
				log.info("用户旧头像为系统内置默认头像，无需删除");
				return;
			}

			String fileName = getFileNameFromUrl(avatarUrl);
			if (fileName != null) {
				// 删除旧头像
				try {
					Path filePath = PathUtil.getExternalPath(fileUploadProperties.getImage().getUploadDir())
							.resolve(fileName).normalize();
					PathUtil.deleteFileOrDirectory(filePath);
				} catch (IOException e) {
					log.error("删除旧头像失败", e);
				}
			} else {
				log.warn("用户头像 URL 格式错误：{}", avatarUrl);
			}
		});
	}

	// 将上传的头像文件从临时目录移动到上传目录
	private void moveAvatar(String avatarUrl) {
		Path source = PathUtil.getExternalPath(fileUploadProperties.getImage().getTempDir())
				.resolve(getFileNameFromUrl(avatarUrl)).normalize();
		Path targetDir = PathUtil.getExternalPath(fileUploadProperties.getImage().getUploadDir());
		try {
			PathUtil.moveFile(source, targetDir);
		} catch (IOException e) {
			log.error("移动上传头像文件失败", e);
		}
	}

	@Override
	public void updateAvatarUrl(String avatarUrl, Long userId) {
		// 异步删除旧头像
		deleteOldAvatar(userId);
		// 将上传的头像文件从临时目录移动到上传目录
		moveAvatar(avatarUrl);
		LambdaUpdateWrapper<UserProfile> wrapper = new LambdaUpdateWrapper<>();
		wrapper.set(UserProfile::getAvatarUrl, avatarUrl);
		this.updateUserProfileByUserId(wrapper);
	}

	// 删除未引用的头像文件
	@Override
	public Map<String, Integer> deleteUnusedAvatarFiles() {
		// 查询数据库中所有头像 URL（去重之后的）
		Set<String> dbUrls = this.baseMapper.selectDistinctAvatarUrls();

		// 要删除文件的目录：上传头像的目录
		final Path avatarBaseDir = PathUtil.getExternalPath(fileUploadProperties.getImage().getUploadDir());

		// 要保留的文件名集合
		Set<String> toKeep = new HashSet<>();
		String fileName;
		for (String url : dbUrls) {
			// 跳过 系统内置默认头像
			if (url.startsWith("/default/avatar/")) continue;
			fileName = getFileNameFromUrl(url);
			if (fileName != null) {
				toKeep.add(fileName);
			}
		}

		// toKeep.forEach(System.out::println);

		// 删除未引用的头像文件
		Map<String, Integer> stat = PathUtil.deleteFilesWithFileName(avatarBaseDir, toKeep);
		// 输出统计信息：总计，已删除文件数量，删除失败文件个数，剩余文件数量
		log.info("删除未引用的头像文件统计信息：总计={}, 已删除={}, 删除失败={}, 剩余={}",
				stat.get("total"), stat.get("success"), stat.get("fail"), stat.get("total") - stat.get("success"));
		return stat;
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
