package com.yubzhou.service.impl;

import com.yubzhou.service.UserProfileService;
import com.yubzhou.util.WebContextUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserProfileServiceImplTest {

	@Autowired
	private UserProfileService userProfileService;

	@Test
	public void getUserProfile() throws Exception {
		long userId = WebContextUtil.getCurrentUserId();
		System.out.println(userProfileService.getProfileByUserId(userId));
	}

	@Test
	public void updateInterests() throws Exception {
		Set<String> interests = new HashSet<>();
		interests.add("游戏");
		interests.add("新闻");
		userProfileService.updateInterests(interests);
	}

	@Test
	public void deleteUnusedAvatarFiles() throws Exception {
		userProfileService.deleteUnusedAvatarFiles();
	}

}