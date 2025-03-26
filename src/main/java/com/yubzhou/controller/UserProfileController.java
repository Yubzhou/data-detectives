package com.yubzhou.controller;

import com.yubzhou.common.RegexpConstant;
import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.model.dto.RegisterUserProfileDto;
import com.yubzhou.model.dto.UpdateUserPasswordDto;
import com.yubzhou.model.po.UserProfile;
import com.yubzhou.service.UserProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/profile")
@Validated
@Slf4j
public class UserProfileController {

	private final UserProfileService userProfileService;

	@Autowired
	public UserProfileController(UserProfileService userProfileService) {
		this.userProfileService = userProfileService;
	}

	@GetMapping("")
	public Result<?> getProfileByUserId() {
		UserProfile profile = userProfileService.getProfileByUserId();
		if (profile == null) {
			return Result.fail(ReturnCode.USER_NOT_FOUND.getCode(), "获取个人信息失败：账号不存在");
		}
		return Result.success(profile);
	}

	// 注册之后调用此接口注册用户部分信息
	@PostMapping("/register")
	public Result<Void> updateInterests(@Valid @RequestBody RegisterUserProfileDto dto) {
		// 转换dto到entity
		UserProfile userProfile = dto.toEntity(dto);
		// 保存到数据库
		userProfileService.updateUserProfile(userProfile);
		return Result.successWithMessage("更新个人信息成功");
	}

	@PostMapping("/interests")
	public Result<Void> updateInterests(@RequestParam("interests")
										@NotEmpty(message = "感兴趣领域不能为空")
										Set<String> interests) {
		// 保存到数据库
		userProfileService.updateInterests(interests);
		return Result.successWithMessage("更新感兴趣领域成功");
	}

	@PostMapping("/avatar")
	public Result<Void> updateAvatarUrl(@RequestParam("avatarUrl")
										@NotBlank(message = "头像url不能为空")
										String avatarUrl) {
		// 保存到数据库
		userProfileService.updateAvatarUrl(avatarUrl);
		return Result.successWithMessage("更新头像成功");
	}

	@PostMapping("/nickname")
	public Result<Void> updateNickname(@RequestParam("nickname")
									   @Length(min = 2, max = 15, message = "昵称长度必须在2-15之间")
									   String nickname) {
		// 保存到数据库
		userProfileService.updateNickname(nickname);
		return Result.successWithMessage("修改昵称成功");
	}

	@PostMapping("/password")
	public Result<Void> updatePassword(@Valid @RequestBody UpdateUserPasswordDto dto) {
		// 保存到数据库
		userProfileService.updatePassword(dto);
		return Result.successWithMessage("修改密码成功");
	}

	@PostMapping("/phone")
	public Result<Void> updatePhone(@RequestParam("phone")
									@Pattern(regexp = RegexpConstant.PHONE, message = RegexpConstant.PHONE_MESSAGE)
									String phone,
									@RequestParam("captcha")
									@Pattern(regexp = RegexpConstant.CAPTCHA, message = RegexpConstant.CAPTCHA_MESSAGE)
									String captcha) {
		// 保存到数据库
		userProfileService.updatePhone(phone, captcha);
		return Result.successWithMessage("更换手机号成功");
	}
}
