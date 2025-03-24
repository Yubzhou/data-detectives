package com.yubzhou.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties {
	private String accessKeyId;
	private String accessKeySecret;
	private String signName;
	private String loginTemplateCode;
	private String registerTemplateCode;
	private String endpoint;

	@Getter
	@AllArgsConstructor
	public enum TemplateCode {
		LOGIN,
		REGISTER;

		public static TemplateCode from(String code) {
			try {
				return TemplateCode.valueOf(code.toUpperCase());
			} catch (Exception e) {
				return null;
			}
		}

		public String getCode(AliyunSmsProperties properties) {
			return switch (this) {
				case LOGIN -> properties.getLoginTemplateCode();
				case REGISTER -> properties.getRegisterTemplateCode();
			};
		}
	}
}