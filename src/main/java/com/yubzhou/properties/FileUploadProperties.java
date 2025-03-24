package com.yubzhou.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadProperties {

	private Image image;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Image {
		private String uploadDir;
		private String accessUrl;
	}
}