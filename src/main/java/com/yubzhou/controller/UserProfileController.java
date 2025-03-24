package com.yubzhou.controller;

import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.model.dto.RegisterUserProfileDto;
import com.yubzhou.model.po.UserProfile;
import com.yubzhou.service.UserProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
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
			return Result.fail(ReturnCode.USER_NOT_FOUND);
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
}
