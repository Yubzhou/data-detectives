package com.yubzhou.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadProperties {

	private MyFileType image;
	private MyFileType json;


	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MyFileType {
		private String uploadDir;
		private String accessUrl;
	}
}