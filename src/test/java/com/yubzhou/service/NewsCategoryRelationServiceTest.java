package com.yubzhou.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
class NewsCategoryRelationServiceTest {

	@Autowired
	private NewsCategoryRelationService newsCategoryRelationService;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	public void test01() throws Exception {
		newsCategoryRelationService.cacheAllNewsCategoryRelationToRedis();
	}

	@Test
	public void test02() throws Exception {
		List<Long> newsIds = List.of(70L, 71L, 72L, 73L, 74L, 75L, 76L, 77L, 78L, 79L, 80L);
		Object[] newsIdsArray = newsIds.toArray();

		long start = System.currentTimeMillis();
		Map<Long, List<String>> newsCategoryRelationMap = newsCategoryRelationService.getNewsCategoryRelationMap(newsIds);
		long end = System.currentTimeMillis();
		newsCategoryRelationMap.forEach((newsId, categoryNames) -> {
			System.out.println("newsId: " + newsId + ", categoryNames: " + categoryNames);
		});
		log.info("getNewsCategoryRelationMap cost {} ms", end - start);
	}
}