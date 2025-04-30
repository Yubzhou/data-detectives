package com.yubzhou.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadProperties {

	private FileUploadType image;
	private FileUploadType json;


	@Getter
	@Setter
	@ToString
	public static class FileUploadType {
		private String uploadDir;
		private String accessUrl;
		private String tempDir;
	}
}