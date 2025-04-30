package com.yubzhou.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "myapp.cors")
public class CorsProperties {
	private String[] allowedOriginPatterns;
}