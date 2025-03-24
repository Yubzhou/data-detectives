package com.yubzhou.service.impl;

import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.aliyun.teautil.models.RuntimeOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.properties.AliyunSmsProperties;
import com.yubzhou.properties.AliyunSmsProperties.TemplateCode;
import com.yubzhou.util.LocalAssert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AliyunSmsService {

	private final com.aliyun.dysmsapi20170525.Client smsClient;
	private final AliyunSmsProperties smsProperties;
	private final ObjectMapper mapper;

	/**
	 * 发送短信验证码
	 *
	 * @param phoneNumber  手机号
	 * @param code         数字验证码
	 * @param expireTime   验证码过期时间（分钟）
	 * @param templateCode 短信模板代码
	 */
	public void sendSms(String phoneNumber, String code, int expireTime, TemplateCode templateCode) {
		LocalAssert.hasText(phoneNumber, "手机号不能为空");
		LocalAssert.assertSmsCodeFormat(code, "验证码需为6位数字");
		LocalAssert.assertPositive(expireTime, "验证码过期时间必须为正整数");

		templateCode = templateCode != null ? templateCode : TemplateCode.LOGIN;

		try {
			String templateParamJson = this.generateTemplateParamJson(code, expireTime);
			SendSmsRequest sendSmsRequest = buildSendSmsRequest(phoneNumber, smsProperties.getSignName(), templateCode.getCode(smsProperties), templateParamJson);

			SendSmsResponse sendSmsResponse = smsClient.sendSmsWithOptions(sendSmsRequest, new RuntimeOptions());
			SendSmsResponseBody body = sendSmsResponse.getBody();
			if (!"OK".equalsIgnoreCase(body.getCode()) || !"OK".equalsIgnoreCase(body.getMessage())) {
				log.warn("向手机号：{} 发送短信验证码失败，返回码：{}，返回信息：{}", phoneNumber, body.getCode(), body.getMessage());
				throw new BusinessException(500, "短信验证码发送失败：" + body.getMessage());
			}
			log.info("向手机号：{} 发送短信验证码成功！", phoneNumber);
		} catch (Exception e) {
			log.error("发送短信验证码异常：", e);
			if (e instanceof BusinessException) throw (BusinessException) e;
			throw new BusinessException(500, "短信验证码发送异常：" + e.getMessage());
		}
	}

	private SendSmsRequest buildSendSmsRequest(String phoneNumber, String signName, String templateCode, String templateParamJson) {
		return new SendSmsRequest()
				.setPhoneNumbers(phoneNumber)
				.setSignName(signName)
				.setTemplateCode(templateCode)
				.setTemplateParam(templateParamJson);
	}

	private String generateTemplateParamJson(String code, int expireTime) throws JsonProcessingException {
		Map<String, Object> templateParam = new HashMap<>();
		templateParam.put("code", code);
		templateParam.put("time", expireTime);
		return mapper.writeValueAsString(templateParam);
	}
}
