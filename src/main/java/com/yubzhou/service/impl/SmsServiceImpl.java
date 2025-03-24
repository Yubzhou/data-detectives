package com.yubzhou.service.impl;

import com.yubzhou.common.RedisConstant;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.service.SmsService;
import com.yubzhou.util.ClientFingerprintUtil;
import com.yubzhou.util.RandomUtil;
import com.yubzhou.util.RedisUtil;
import com.yubzhou.properties.AliyunSmsProperties.TemplateCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

	private final RedisUtil redisUtil;
	private final AliyunSmsService aliyunSmsService;


	/**
	 * 调用短信服务API发送短信
	 *
	 * @param phone      String 手机号
	 * @param code       int 数字验证码
	 * @param expireTime int 验证码过期时间（分钟）
	 */
	@Override
	public void sendSms(String phone, String code, int expireTime, TemplateCode templateCode) {
		aliyunSmsService.sendSms(phone, code, expireTime, templateCode);
	}

	@Override
	public void sendSmsCaptcha(String phone, TemplateCode templateCode, HttpServletRequest request) {
		// 限流：防止单个手机号码在60秒内重复发送验证码（例如：60秒内不能重复发送）
		String smsLimitKey = RedisConstant.SMS_LIMIT_PREFIX + phone;
		if (redisUtil.hasKey(smsLimitKey)) {
			throw new BusinessException(ReturnCode.RC429);
		}

		// 限流：每日限流（IP地址限流），单个IP地址每天只能发送10次
		String clientIp = ClientFingerprintUtil.getClientIp(request);
		String ipLimitKey = RedisConstant.SMS_DAILY_LIMIT_PREFIX + clientIp;
		this.throttle(ipLimitKey,
				10,
				RedisConstant.SMS_DAILY_LIMIT_EXPIRE_TIME,
				"该IP地址今日短信验证码发送次数已达上限，请24小时后再试");

		// 限流：每日限流（手机号限流），单个手机号码每天只能发送5次
		String smsDailyLimitKey = RedisConstant.SMS_DAILY_LIMIT_PREFIX + phone;
		this.throttle(smsDailyLimitKey,
				5,
				RedisConstant.SMS_DAILY_LIMIT_EXPIRE_TIME,
				"今日短信验证码发送次数已达上限，请24小时后再试");

		// 生成6位随机验证码
		String captcha = RandomUtil.generateRandomNumber(6);

		// 存储到Redis，设置5分钟过期
		String smsCaptchaKey = RedisConstant.SMS_CAPTCHA_PREFIX + phone;
		redisUtil.set(smsCaptchaKey, captcha, RedisConstant.SMS_CAPTCHA_EXPIRE_TIME);

		// 调用短信服务商API发送短信（需处理异常）
		try {
			this.sendSms(phone, captcha, 5, templateCode);
		} catch (Exception e) {
			redisUtil.del(smsCaptchaKey); // 发送失败删除验证码
			if (e instanceof BusinessException) throw (BusinessException) e;
			throw new BusinessException(ReturnCode.RC500.getCode(), "短信验证码发送失败，请稍后再试");
		}

		// 设置限流锁（60秒）
		redisUtil.set(smsLimitKey, true, RedisConstant.SMS_LIMIT_EXPIRE_TIME);
	}

	/**
	 * 限流辅助函数
	 *
	 * @param key        String redis key
	 * @param limit      long 限流阈值
	 * @param expireTime long 过期时间（秒）
	 * @param message    String 限流提示信息
	 */
	private void throttle(String key, long limit, long expireTime, String message) {
		long count = redisUtil.incr(key, 1);
		if (count == 1L) { // 当count为1时，redis中不存在该key，说明是第一次登录，设置过期时间
			redisUtil.expire(key, expireTime);
		}
		if (count > limit) {
			throw new BusinessException(ReturnCode.RC429.getCode(), message);
		}
	}
}
