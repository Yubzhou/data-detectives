package com.yubzhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.common.UserToken;
import com.yubzhou.model.dto.LoginUserDto;
import com.yubzhou.model.po.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface UserService extends IService<User> {
	Map<String, String> register(User user, String captcha, HttpServletRequest request);

	Map<String, String> loginWithCaptcha(User loginUser, String captcha, HttpServletRequest request);

	Map<String, String> loginWithPassword(User loginUser, HttpServletRequest request);

	void logout(UserToken userToken);

	Map<String, String> refreshToken(String encryptedRefreshToken, HttpServletRequest request);
}
