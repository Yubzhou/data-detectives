package com.yubzhou.service.impl;

import com.yubzhou.service.NewsCategoryRelationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class NewsCategoryRelationServiceImplTest {

	@Autowired
	private NewsCategoryRelationService newsCategoryRelationService;

	@Test
	public void testGetNewsCount() {
		long categoryId = 0L;
		long count = newsCategoryRelationService.getNewsCount(categoryId);
		log.info("categoryId = {}, count = {}", categoryId, count);

		categoryId = 1L;
		count = newsCategoryRelationService.getNewsCount(categoryId);
		log.info("categoryId = {}, count = {}", categoryId, count);
	}

}