package com.yubzhou.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "myapp.cors")
public class CorsProperties {
	private String[] allowedOriginPatterns;
}