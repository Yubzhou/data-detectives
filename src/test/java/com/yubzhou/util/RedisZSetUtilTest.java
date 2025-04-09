package com.yubzhou.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
class RedisZSetUtilTest {
	@Autowired
	private RedisZSetUtil redisZSetUtil;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@BeforeEach
	void setup() {
		Random random = new Random();
		int num;

		redisTemplate.delete(Arrays.asList("testZSetString", "excludeSetString", "testZSetLong", "excludeSetLong"));


		// 准备测试数据
		// for (int i = 1; i <= 10000; i++) {
		// 	redisTemplate.opsForZSet().add("testZSetString", String.valueOf(i), i);
		// }
		// for (int i = 1; i <= 100; i++) {
		// 	num = random.nextInt(1, 10001);
		// 	redisTemplate.opsForSet().add("excludeSetString", String.valueOf(num));
		// }
		//
		//
		// for (int i = 1; i <= 10000; i++) {
		// 	redisTemplate.opsForZSet().add("testZSetLong", i, i);
		// }
		// for (int i = 1; i <= 100; i++) {
		// 	num = random.nextInt(1, 10001);
		// 	redisTemplate.opsForSet().add("excludeSetLong", num);
		// }

		for (int i = 1; i <= 10; i++) {
			redisTemplate.opsForZSet().add("testZSetString", String.valueOf(i), i);
		}
		for (int i = 1; i <= 3; i++) {
			redisTemplate.opsForSet().add("excludeSetString", String.valueOf(i));
		}


		for (int i = 1; i <= 10; i++) {
			redisTemplate.opsForZSet().add("testZSetLong", i, i);
		}
		for (int i = 1; i <= 3; i++) {
			redisTemplate.opsForSet().add("excludeSetLong", i);
		}
	}

	@Test
	public void test() throws Exception {
		System.out.println("测试...");
	}

	@Test
	void testGetRandomExcludingString() {
		long startTime = System.currentTimeMillis();
		List<String> result = redisZSetUtil.getRandomZSetStrings(
				"testZSetString",
				"excludeSetString",
				1L,
				10
		);
		long endTime = System.currentTimeMillis();

		log.info("result: {}, cost {} ms", result, endTime - startTime);

		assertEquals(10, result.size());
	}

	@Test
	void testGetRandomExcludingLong() {
		long startTime = System.currentTimeMillis();
		List<Long> result = redisZSetUtil.getRandomZSetLongs(
				"testZSetLong",
				"excludeSetLong",
				1L,
				10
		);
		long endTime = System.currentTimeMillis();

		log.info("result: {}, cost {} ms", result, endTime - startTime);

		assertEquals(10, result.size());
	}

	// @AfterEach
	// void tearDown() {
	// 	redisTemplate.delete(Arrays.asList("testZSetString", "excludeSetString", "testZSetLong", "excludeSetLong"));
	// }
}