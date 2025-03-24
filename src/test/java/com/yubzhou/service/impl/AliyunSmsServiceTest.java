package com.yubzhou.service.impl;


import com.yubzhou.properties.AliyunSmsProperties;
import com.yubzhou.util.RandomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AliyunSmsServiceTest {

	@Autowired
	private AliyunSmsService aliyunSmsService;


	@Test
	public void test01() throws Exception {
		String code = RandomUtil.generateRandomNumber(6);
		// aliyunSmsService.sendSms("18607912978", code, 5, AliyunSmsProperties.TemplateCode.LOGIN);
		aliyunSmsService.sendSms("19324887049", code, 5, AliyunSmsProperties.TemplateCode.LOGIN);
	}

}