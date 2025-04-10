package com.yubzhou.service.init;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDataGeneratorTest {

	@Autowired
	private UserDataGenerator userDataGenerator;

	@Test
	public void testBatchInsertUserAndProfile() throws Exception {
		userDataGenerator.batchInsertUserAndProfile();
	}

}