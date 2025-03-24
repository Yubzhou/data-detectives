package com.yubzhou.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.common.*;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.mapper.UserMapper;
import com.yubzhou.model.po.User;
import com.yubzhou.service.UserService;
import com.yubzhou.util.BCryptUtil;
import com.yubzhou.util.JwtUtil;
import com.yubzhou.util.LocalAssert;
import com.yubzhou.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
	private final JwtUtil jwtUtil;
	private final RedisUtil redisUtil;

	@Autowired
	public UserServiceImpl(JwtUtil jwtUtil, RedisUtil redisUtil) {
		this.jwtUtil = jwtUtil;
		this.redisUtil = redisUtil;
	}

	/**
	 * 注册用户
	 * 注册成功默认自动登录，生成访问令牌和刷新令牌
	 *
	 * @param user 用户信息
	 */
	@Override
	public Map<String, String> register(User user, String captcha, HttpServletRequest request) {
		// 如果校验失败会抛出BusinessException
		smsCaptchaHandler(user, captcha);

		user.setRole(UserRole.USER); // 默认注册为普通用户
		user.setStatus(UserStatus.NORMAL); // 默认注册为正常状态
		String plainPassword = user.getPassword(); // 获取原始密码
		// 只有密码不为空时才加密密码
		if (plainPassword != null) {
			String hashedPassword = BCryptUtil.encode(plainPassword); // 使用BCrypt加密密码
			user.setPassword(hashedPassword);
		}
		boolean result = this.save(user); // 插入用户信息到数据库
		if (!result) {
			throw new BusinessException(ReturnCode.RC500.getCode(), "注册失败：系统异常，请联系管理员"); // 注册失败
		}

		// 注册成功，返回双token
		return this.generateTokens(user, request);
	}

	private User findByPhone(String phone) {
		return this.lambdaQuery()
				// 只查询用户ID、手机号、密码、角色、状态
				.select(User::getId, User::getPhone, User::getPassword, User::getRole, User::getStatus)
				.eq(User::getPhone, phone) // 根据手机号查询
				.one(); // 获取查询结果
	}

	private void updateLastLoginAt(User user) {
		this.lambdaUpdate()
				.set(User::getLastLoginAt, LocalDateTime.now())
				.eq(User::getId, user.getId())
				.update();
	}

	private Map<String, String> generateTokens(User user, HttpServletRequest request) {
		// 获取当前请求的IP和User-Agent来生成fingerprint
		String fingerprint = UserToken.generateFingerprint(request);
		UserToken userToken = UserToken.of(user.getId(), user.getRole(), fingerprint);
		String accessToken = jwtUtil.generateAccessToken(userToken);
		String rawRefreshToken = jwtUtil.generateRefreshToken(userToken);
		String encryptedRefreshToken = jwtUtil.generateEncryptedRefreshToken(rawRefreshToken);
		if (!StringUtils.hasText(accessToken)
				|| !StringUtils.hasText(rawRefreshToken)
				|| !StringUtils.hasText(encryptedRefreshToken)) {
			log.error("token生成失败：系统异常，请联系管理员");
			throw new BusinessException(ReturnCode.TOKEN_GENERATE_ERROR.getCode(), "token生成失败：系统异常，请联系管理员"); // 令牌生成失败
		}
		// 将rawRefreshToken存入redis缓存中
		this.setRefreshTokenToRedis(user.getId(), rawRefreshToken);
		// 登陆成功，返回访问令牌和刷新令牌
		return Map.of("accessToken", accessToken, "refreshToken", encryptedRefreshToken);
	}

	private void smsCaptchaHandler(User user, String captcha) throws BusinessException {
		// 检查账号是否被锁定（验证码错误次数限制，如5次错误后锁定1小时）
		String smsLockKey = RedisConstant.SMS_LOCK_PREFIX + user.getPhone();
		if (redisUtil.hasKey(smsLockKey)) {
			throw new BusinessException(403, "登录验证码错误次数过多，账号验证码登录功能已锁定，请1小时后重试"); // 账号被锁定
		}
		// 从Redis获取验证码
		String smsCaptchaKey = RedisConstant.SMS_CAPTCHA_PREFIX + user.getPhone();
		String storedCaptcha = (String) redisUtil.get(smsCaptchaKey);

		// Yubzhou TODO 2025/3/21 11:17; 测试使用：先将验证码定死为123456
		storedCaptcha = "123456";

		// 验证码校验
		if (storedCaptcha == null) {
			// 验证码已过期
			throw new BusinessException(ReturnCode.RC400.getCode(), "验证码已过期");
		}

		// 验证码错误（1小时内错误次数超过5次，则锁定验证码登录功能1小时）
		String smsErrorCountKey = RedisConstant.SMS_ERROR_COUNT_PREFIX + user.getPhone();
		if (!Objects.equals(storedCaptcha, captcha)) {
			// 错误计数器+1
			long errorCount = redisUtil.incr(smsErrorCountKey, 1);

			// 首次错误设置过期时间（1小时自动清理）
			if (errorCount == 1L) {
				redisUtil.expire(smsErrorCountKey, RedisConstant.SMS_LOCK_EXPIRE_TIME);
			}

			// 错误超过5次触发锁定（1小时后自动解锁）
			if (errorCount >= 5) {
				redisUtil.set(smsLockKey, true, RedisConstant.SMS_LOCK_EXPIRE_TIME);
				redisUtil.del(smsErrorCountKey); // 清除计数器
				throw new BusinessException(403, "登录验证码错误次数过多，账号验证码登录功能已锁定，请1小时后重试");
			}

			throw new BusinessException(400, "登录验证码错误，剩余尝试次数：" + (5 - errorCount)); // 验证码错误
		}

		// 验证成功后，删除验证码，清理安全状态
		redisUtil.del(smsCaptchaKey, smsLockKey, smsErrorCountKey);
	}

	/**
	 * 登录用户
	 *
	 * @param loginUser 登录用户信息
	 * @return 访问令牌和刷新令牌
	 */
	@Override
	public Map<String, String> loginWithPassword(User loginUser, HttpServletRequest request) {
		// // 构造查询条件
		// LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
		// queryWrapper
		// 		.select(User::getPhone, User::getPassword, User::getRole) // 只查询手机号、密码、角色
		// 		.eq(User::getPhone, loginUser.getPhone()); // 根据手机号查询
		// User user = this.getOne(queryWrapper); // 获取查询结果
		User user = this.findByPhone(loginUser.getPhone());
		// 验证手机号是否存在，以及密码是否正确
		boolean isMatch = user != null && BCryptUtil.matches(loginUser.getPassword(), user.getPassword());
		if (!isMatch) {
			throw new BusinessException(ReturnCode.USERNAME_OR_PASSWORD_ERROR); // 登录失败
		}

		switch (user.getStatus()) {
			case DISABLED:
				throw new BusinessException(ReturnCode.USER_DISABLED); // 账号被禁用
			case LOCKED:
				throw new BusinessException(ReturnCode.USER_LOCKED); // 账号被锁定
			case LOGOUT:
				throw new BusinessException(ReturnCode.USER_LOGOUT); // 账号已注销
		}

		// 更新上次登录时间
		this.updateLastLoginAt(user);

		// 登录成功，返回双token
		return this.generateTokens(user, request);
	}

	public Map<String, String> loginWithCaptcha(User loginUser, String captcha, HttpServletRequest request) {
		// 如果校验失败会抛出BusinessException
		smsCaptchaHandler(loginUser, captcha);

		// 查询用户
		User user = this.findByPhone(loginUser.getPhone());
		if (user == null) {
			throw new BusinessException(ReturnCode.USER_NOT_FOUND); // 账号不存在
		}

		// 更新上次登录时间
		this.updateLastLoginAt(user);

		// 登录成功，返回双token
		return this.generateTokens(user, request);
	}


	/**
	 * 登出用户，删除（或禁用）用户的刷新令牌
	 *
	 * @param userToken 用户相关信息
	 */
	@Override
	public void logout(UserToken userToken) {
		// 登出，删除用户的刷新令牌
		this.deleteRefreshTokenFromRedis(userToken.getUserId());
	}

	/**
	 * 携带加密的refreshToken同时刷新accessToken和refreshToken
	 *
	 * @param encryptedRefreshToken 刷新令牌
	 * @return 访问令牌和刷新令牌
	 */
	@Override
	public Map<String, String> refreshToken(String encryptedRefreshToken, HttpServletRequest request) {
		// 验证refreshToken是否为空
		LocalAssert.assertTokenHasText(encryptedRefreshToken, "refreshToken不能为空");

		// 验证refreshToken是否有效
		DecodedJWT decodedJWT = jwtUtil.verifyEncryptedRefreshToken(encryptedRefreshToken);
		if (decodedJWT == null) {
			throw new BusinessException(ReturnCode.INVALID_TOKEN.getCode(), "refreshToken无效"); // 刷新失败，refreshToken无效
		}

		// 获取refreshToken中用户相关的自定义声明
		UserToken userToken = decodedJWT.getClaim(JwtUtil.USER_CLAIM_KEY).as(UserToken.class);

		// 获取到用户传递过来的原始refreshToken（未加密的）
		String rawRefreshToken = decodedJWT.getToken();
		// 从redis缓存中获取该用户的refreshToken（未加密的）
		String redisRefreshToken = this.getRefreshTokenFromRedis(userToken.getUserId());
		if (!Objects.equals(redisRefreshToken, rawRefreshToken)) {
			throw new BusinessException(ReturnCode.INVALID_TOKEN.getCode(), "refreshToken无效"); // 刷新失败，refreshToken无效
		}

		// 如果redis中的与refreshToken一致，判断当前请求的fingerprint是否与refreshToken的fingerprint一致
		// 获取当前请求的IP和User-Agent来生成fingerprint
		String currentFingerprint = UserToken.generateFingerprint(request);
		// 如果fingerprint不一致，则返回刷新失败
		if (!Objects.equals(userToken.getFingerprint(), currentFingerprint)) {
			throw new BusinessException(ReturnCode.INVALID_TOKEN.getCode(), "refreshToken无效"); // 刷新失败，refreshToken无效
		}

		// 如果之前逻辑正确，则删除或禁用之前的refreshToken，替换为新的refreshToken
		this.deleteRefreshTokenFromRedis(userToken.getUserId());

		// 基于当前请求的fingerprint，来更新userToken里面的fingerprint，生成新的accessToken和refreshToken
		userToken.setFingerprint(currentFingerprint);
		String newAccessToken = jwtUtil.generateAccessToken(userToken);
		String newRawRefreshToken = jwtUtil.generateRefreshToken(userToken);
		String newEncryptedRefreshToken = jwtUtil.generateEncryptedRefreshToken(newRawRefreshToken);

		// 将新生成的rawRefreshToken存入redis缓存中
		this.setRefreshTokenToRedis(userToken.getUserId(), newRawRefreshToken);

		if (log.isInfoEnabled()) {
			log.info("账号token刷新成功! userId:{}", userToken.getUserId());
		}

		return Map.of("accessToken", newAccessToken, "refreshToken", newEncryptedRefreshToken); // 返回访问令牌和刷新令牌
	}

	private void setRefreshTokenToRedis(Long userId, String refreshToken) {
		if (userId == null) {
			log.warn("setRefreshTokenToRedis方法传入的 userId 为空，不执行设置操作");
			return;
		}
		redisUtil.set(RedisConstant.USER_REFRESH_TOKEN_PREFIX + userId,
				refreshToken,
				RedisConstant.USER_REFRESH_TOKEN_EXPIRE_TIME);
	}

	private String getRefreshTokenFromRedis(Long userId) {
		return (String) redisUtil.get(RedisConstant.USER_REFRESH_TOKEN_PREFIX + userId);
	}

	private void deleteRefreshTokenFromRedis(Long userId) {
		if (userId == null) {
			log.warn("deleteRefreshTokenFromRedis方法传入的 userId 为空，不执行删除操作");
			return;
		}
		redisUtil.del(RedisConstant.USER_REFRESH_TOKEN_PREFIX + userId);
	}
}
