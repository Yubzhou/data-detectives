package com.yubzhou.util;

import com.yubzhou.properties.FileUploadProperties;
import com.yubzhou.service.impl.UserProfileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;

@SpringBootTest
@Slf4j
class PathUtilTest {

	@Autowired
	private FileUploadProperties fileUploadProperties;
	@Autowired
	private UserProfileServiceImpl  userProfileService;

	@Test
	public void test01() throws Exception {
		Path externalPath = PathUtil.getExternalPath(fileUploadProperties.getImage().getUploadDir());
		log.info(externalPath.toString());
		String fileName = "test.jpg";
		log.info(externalPath.resolve(fileName).normalize().toString());
	}

	@Test
	public void test02() throws Exception {
		String avatarUrl = "/uploads/images/a97fec1735c84c1abbc31ba4bdfa6db9.png";
		Path source = PathUtil.getExternalPath(fileUploadProperties.getImage().getTempDir())
				.resolve(userProfileService.getFileNameFromUrl(avatarUrl)).normalize();
		Path targetDir = PathUtil.getExternalPath(fileUploadProperties.getImage().getUploadDir());
		log.info(source.toString());
		log.info(targetDir.toString());
	}



}