package com.yubzhou.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
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
		LOGIN, // 登录模板
		REGISTER, // 注册模板
		VERIFY // 验证手机号码模板
		;

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
				default -> properties.getLoginTemplateCode(); // 如果为其他模板，则使用登录模板
			};
		}
	}
}