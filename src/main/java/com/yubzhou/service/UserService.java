package com.yubzhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.common.MinAndMaxId;
import com.yubzhou.common.UserToken;
import com.yubzhou.model.po.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;

import java.util.Map;

public interface UserService extends IService<User> {
	MinAndMaxId getMinAndMaxId();

	User findByUserId(@NonNull Long userId);

	Map<String, String> register(User user, String captcha, HttpServletRequest request);

	Map<String, String> loginWithCaptcha(User loginUser, String captcha, HttpServletRequest request);

	Map<String, String> loginWithPassword(User loginUser, HttpServletRequest request);

	void logout(UserToken userToken);

	Map<String, String> refreshToken(String encryptedRefreshToken, HttpServletRequest request);

	void updatePassword(Long userId, String oldPassword, String newPassword);

	void updatePhone(long userId, String phone, String captcha);
}
