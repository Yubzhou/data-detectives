package com.yubzhou.config;

import com.aliyun.teaopenapi.models.Config;
import com.yubzhou.properties.AliyunSmsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunSmsConfig {

	private final AliyunSmsProperties smsProperties;

	public AliyunSmsConfig(AliyunSmsProperties smsProperties) {
		this.smsProperties = smsProperties;
	}

	@Bean
	public com.aliyun.dysmsapi20170525.Client smsClient() throws Exception {
		Config config = new Config()
				.setAccessKeyId(smsProperties.getAccessKeyId())
				.setAccessKeySecret(smsProperties.getAccessKeySecret())
				.setEndpoint(smsProperties.getEndpoint());
		return new com.aliyun.dysmsapi20170525.Client(config);
	}
}