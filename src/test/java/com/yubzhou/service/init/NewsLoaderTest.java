package com.yubzhou.service.init;

import com.yubzhou.service.init.NewsLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

	@Test
	public void testUpdateNewsCovers() throws Exception {
		newsLoader.updateNewsCovers();
	}
}