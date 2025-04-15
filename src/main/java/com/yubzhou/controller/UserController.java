package com.yubzhou.controller;

import com.yubzhou.annotation.JwtIgnore;
import com.yubzhou.common.RegexpConstant;
import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.common.UserToken;
import com.yubzhou.model.dto.LoginUserDto;
import com.yubzhou.model.dto.RegisterUserDto;
import com.yubzhou.model.po.User;
import com.yubzhou.properties.AliyunSmsProperties.TemplateCode;
import com.yubzhou.service.SmsService;
import com.yubzhou.service.UserService;
import com.yubzhou.util.PhoneUtil;
import com.yubzhou.util.WebContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
	private final UserService userService;
	private final SmsService smsService;

	/**
	 * 用户注册
	 *
	 * @param registerUserDto 用户注册信息
	 * @return 注册成功信息
	 */
	@JwtIgnore // 自定义注解，忽略JWT验证
	@PostMapping("/users/register")
	public Result<?> register(@Valid @RequestBody RegisterUserDto registerUserDto,
							  HttpServletRequest request) {
		// 拷贝dto到po
		User registerUser = RegisterUserDto.toEntity(registerUserDto);
		// 用户注册
		Map<String, String> tokens = userService.register(registerUser, registerUserDto.getCaptcha(), request);
		if (log.isInfoEnabled()) {
			log.info("账号注册成功! phone:{}", registerUser.getPhone());
		}
		return Result.success("注册成功", Map.of("tokens", tokens));
	}

	/**
	 * 用户登录（手机号+短信验证码 登录接口）
	 *
	 * @param loginUserDto 用户登录信息
	 * @return 登录成功信息
	 */
	@JwtIgnore // 自定义注解，忽略JWT验证
	@PostMapping("/users/login/captcha")
	public Result<?> loginWithCaptcha(@Validated(LoginUserDto.CaptchaLogin.class) @RequestBody LoginUserDto loginUserDto,
									  HttpServletRequest request) {
		// 拷贝dto到po
		User loginUser = LoginUserDto.toEntity(loginUserDto);
		// 登录
		Map<String, String> tokens = userService.loginWithCaptcha(loginUser, loginUserDto.getCaptcha(), request);
		if (log.isInfoEnabled()) {
			log.info("账号登录成功! phone:{}", loginUserDto.getPhone());
		}
		return Result.success("登录成功", Map.of("tokens", tokens));
	}

	/**
	 * 用户登录（手机号+密码 登录接口）
	 *
	 * @param loginUserDto 用户登录信息
	 * @return 登录成功信息
	 */
	@JwtIgnore // 自定义注解，忽略JWT验证
	@PostMapping("/users/login/password")
	public Result<?> loginWithPassword(@Validated(LoginUserDto.PasswordLogin.class) @RequestBody LoginUserDto loginUserDto,
									   HttpServletRequest request) {
		// 拷贝dto到po
		User loginUser = LoginUserDto.toEntity(loginUserDto);
		// 登录
		Map<String, String> tokens = userService.loginWithPassword(loginUser, request);
		if (log.isInfoEnabled()) {
			log.info("账号登录成功! phone:{}", loginUser.getPhone());
		}
		return Result.success("登录成功", Map.of("tokens", tokens));
	}

	/**
	 * 用户登出：需要携带accessToken
	 *
	 * @return 登出成功信息
	 */
	@PostMapping("/users/logout")
	public Result<Void> logout() {
		// 获取当前用户的相关信息
		UserToken userToken = WebContextUtil.getUserToken();
		// 登出
		userService.logout(userToken);
		if (log.isInfoEnabled()) {
			log.info("账号登出成功! userId:{}", userToken.getUserId());
		}
		return Result.successWithMessage("登出成功");
	}

	/**
	 * 刷新token令牌（携带refreshToken同时刷新accessToken和refreshToken）
	 * 无需携带accessToken，只需携带refreshToken
	 *
	 * @param refreshToken 刷新token
	 * @param request      用于获取当前用户的IP地址和User-Agent
	 * @return 刷新成功信息 和 新的双token
	 */
	@JwtIgnore // 自定义注解，忽略JWT验证
	@PostMapping("/auth/refresh")
	public Result<Map<String, Map<String, String>>> refresh(@RequestParam("refreshToken") String refreshToken,
															HttpServletRequest request) {
		// 刷新token
		Map<String, String> tokens = userService.refreshToken(refreshToken, request);
		return Result.success("token刷新成功", Map.of("tokens", tokens));
	}

	@JwtIgnore // 自定义注解，忽略JWT验证
	@PostMapping("/sms/send")
	public Result<Void> sendSmsCaptcha(
			@Pattern(regexp = RegexpConstant.PHONE, message = RegexpConstant.PHONE_MESSAGE)
			@RequestParam("phone") String phone,
			@RequestParam("templateCode") String templateCode,
			HttpServletRequest request) {
		// Yubzhou TODO 2025/4/15 17:21; 暂时不支持移动手机号的短信验证码功能
		if (PhoneUtil.isChinaMobile(phone)) {
			return Result.fail(ReturnCode.SERVER_UNAVAILABLE.getCode(),
					"根据工信部短信签名实名制管理的最新要求，手机短信验证码功能暂停服务。");
		}
		TemplateCode template = TemplateCode.from(templateCode);
		// 发送短信验证码
		smsService.sendSmsCaptcha(phone, template, request);
		return Result.successWithMessage("短信验证码发送成功");
	}
}
