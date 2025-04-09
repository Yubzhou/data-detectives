package com.yubzhou.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NewsLoaderTest {

	@Autowired
	private NewsLoader newsLoader;

	@Test
	public void testInsertNewsData() throws Exception {
		newsLoader.insertNewsData();
	}

	@Test
	public void testInsertNewsCategoryRelations() throws Exception {
		newsLoader.insertNewsCategoryRelations();
	}
}