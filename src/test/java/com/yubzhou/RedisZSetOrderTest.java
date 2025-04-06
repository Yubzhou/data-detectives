package com.yubzhou;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class RedisZSetOrderTest {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private static final String TEST_KEY = "test:leaderboard";

	@BeforeEach
	public void setUp() {
		// 清空测试键的数据
		redisTemplate.delete(TEST_KEY);

		// 向有序集合中添加测试数据
		ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
		zSetOps.add(TEST_KEY, "Alice", 100);
		zSetOps.add(TEST_KEY, "Bob", 200);
		zSetOps.add(TEST_KEY, "Charlie", 150);
		zSetOps.add(TEST_KEY, "David", 300);
		zSetOps.add(TEST_KEY, "Eve", 250);
	}

	@Test
	public void testZReverseRangeWithScoresOrder() {
		// 获取前 5 个元素及其分数
		Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(TEST_KEY, 0, 4);

		System.out.println("Set Type: " + tuples.getClass().getName()); // java.util.LinkedHashSet

		int i = 1;
		for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
			System.out.println("Rank: " + i + ", Member: " + tuple.getValue() + ", Score: " + tuple.getScore());
			i++;
		}

		assertNotNull(tuples, "Result set should not be null");

		// 转换为列表以便检查顺序
		Object[] sortedValues = tuples.stream()
				.map(ZSetOperations.TypedTuple::getValue)
				.toArray();

		// 预期结果：按分数从高到低排序
		Object[] expectedValues = new Object[]{"David", "Eve", "Bob", "Charlie", "Alice"};

		// 断言结果是否符合预期
		assertArrayEquals(expectedValues, sortedValues, "The result is not in the expected descending order");
	}
}