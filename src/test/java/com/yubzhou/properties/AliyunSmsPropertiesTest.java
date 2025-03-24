package com.yubzhou.properties;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import com.yubzhou.properties.AliyunSmsProperties.TemplateCode;

@SpringBootTest
@Slf4j
class AliyunSmsPropertiesTest {
	@Autowired
	AliyunSmsProperties aliyunSmsProperties;

	@Test
	public void test01() throws Exception {
		String type1 = "LOGIN";
		String type2 = "REGISTER";

		TemplateCode templateCode = TemplateCode.valueOf(type1);
		String code1 = templateCode.getCode(aliyunSmsProperties);
		log.info("code1: {}", code1);
		assertEquals(code1, aliyunSmsProperties.getLoginTemplateCode());

		templateCode = TemplateCode.valueOf(type2);
		String code2 = templateCode.getCode(aliyunSmsProperties);
		log.info("code2: {}", code2);
		assertEquals(code2, aliyunSmsProperties.getRegisterTemplateCode());
	}
}