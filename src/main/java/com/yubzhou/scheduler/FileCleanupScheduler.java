package com.yubzhou.scheduler;

import com.yubzhou.properties.FileUploadProperties;
import com.yubzhou.util.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

// 清空图片上传临时目录定时器
@Component
@RequiredArgsConstructor
@Slf4j
public class FileCleanupScheduler {

	private final FileUploadProperties fileUploadProperties;

	/**
	 * 定时任务，每天凌晨3点清空上传图片临时目录
	 */
	@Scheduled(cron = "0 0 3 * * ?")
	public void cleanExpiredTempFiles() {
		// 获取上传图片临时目录的绝对路径
		Path uploadImageTempDir = PathUtil.getExternalPath(
				fileUploadProperties.getImage().getTempDir()
		);
		try {
			PathUtil.emptyDirectory(uploadImageTempDir);
			log.info("清空上传图片临时目录成功: {}", uploadImageTempDir);
		} catch (IOException e) {
			log.error("清空上传图片临时目录失败: {}", uploadImageTempDir);
		}
	}
}