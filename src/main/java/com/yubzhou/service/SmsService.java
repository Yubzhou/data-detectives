package com.yubzhou.service;

import com.yubzhou.properties.AliyunSmsProperties.TemplateCode;
import jakarta.servlet.http.HttpServletRequest;

public interface SmsService {
	void sendSms(String phone, String code, int expireTime, TemplateCode templateCode);

	void sendSmsCaptcha(String phone, TemplateCode templateCode, HttpServletRequest request);
}
