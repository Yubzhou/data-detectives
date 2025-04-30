package com.yubzhou.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "myapp.keystore")
public class KeyManagerProperties {
	private String path = "./keystore.jks"; // 密钥库路径，默认在当前目录下
	private String password; // 密钥库密码
}