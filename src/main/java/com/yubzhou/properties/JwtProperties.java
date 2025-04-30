package com.yubzhou.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "myapp.jwt")
public class JwtProperties {
	private String secretKey; // 加密密钥
	private long accessTokenExpirationTime = 30 * 60 * 1000; // 访问token过期时间（单位：毫秒）, 默认30分钟
	private long refreshTokenExpirationTime = 5 * 24 * 60 * 60 * 1000; // 刷新token过期时间（单位：毫秒）, 默认5天
	private String tokenPrefix; // token前缀（即前缀为"Bearer "）
	private String tokenHeader; // 认证头（即请求头Authorization）

	/**
	 * 根据是否为访问token获取对应的过期时间
	 *
	 * @param isAccessToken 是否为访问token
	 * @return accessTokenExpirationTime 或 refreshTokenExpirationTime，过期时间（单位：毫秒）
	 */
	public long getTokenExpirationTime(boolean isAccessToken) {
		return isAccessToken ? accessTokenExpirationTime : refreshTokenExpirationTime;
	}
}