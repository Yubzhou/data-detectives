package com.yubzhou.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RedisZSetUtil {

	private final RedisTemplate<String, Object> redisTemplate;
	private final DefaultRedisScript<List> zSetRandomScript;
	private final ObjectMapper objectMapper;

	public RedisZSetUtil(RedisTemplate<String, Object> redisTemplate,
						 @Qualifier("zSetRandomScript") DefaultRedisScript<List> zSetRandomScript,
						 ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.zSetRandomScript = zSetRandomScript;
		this.objectMapper = objectMapper;
	}

	/**
	 * 获取随机元素（支持泛型）
	 *
	 * @param zSetKey       ZSET类型键名
	 * @param excludeSetKey 排除集合键名（Set类型）
	 * @param count         需要获取的数量
	 * @param maxAttempts   最大尝试次数
	 * @param expireSeconds 排除集合的过期时间（秒）
	 * @param elementType   返回元素类型
	 * @return 随机元素列表
	 */
	public <T> List<T> getRandomZSetMembers(String zSetKey,
											String excludeSetKey,
											int count,
											int maxAttempts,
											long expireSeconds,
											Class<T> elementType) {
		LocalAssert.assertPositive(count, "count参数必须大于0");
		LocalAssert.assertPositive(maxAttempts, "maxAttempts参数必须大于0");
		try {
			List<Object> rawResult = redisTemplate.execute(
					zSetRandomScript,
					Arrays.asList(zSetKey, excludeSetKey),
					count, maxAttempts, expireSeconds
			);

			log.info("Raw result from Lua: {}", rawResult);  // 打印原始结果

			return rawResult.stream()
					.map(obj -> objectMapper.convertValue(obj, elementType))
					.toList();
		} catch (RedisSystemException e) {
			throw new BusinessException(ReturnCode.RC500.getCode(), "Redis操作失败");
		}
	}

	/**
	 * 获取随机元素（支持泛型）
	 * 默认尝试次数为3*count，默认排除集合的过期时间为3小时
	 *
	 * @param zSetKey       ZSET类型键名
	 * @param excludeSetKey 排除集合键名（Set类型）
	 * @param count         需要获取的数量
	 * @param elementType   返回元素类型
	 * @return 随机元素列表
	 */
	public <T> List<T> getRandomZSetMembers(String zSetKey,
											String excludeSetKey,
											int count,
											Class<T> elementType) {
		return getRandomZSetMembers(
				zSetKey,
				excludeSetKey,
				count,
				3 * count,
				RedisConstant.NEWS_RECOMMEND_EXPIRE_TIME,
				elementType);
	}

	/**
	 * 简化版String类型获取
	 */
	public List<String> getRandomZSetStrings(String zSetKey,
											 String excludeSetKey,
											 int count) {
		return getRandomZSetMembers(zSetKey, excludeSetKey, count, String.class);
	}

	/**
	 * 简化版Integer类型获取
	 */
	public List<Integer> getRandomZSetIntegers(String zSetKey,
											   String excludeSetKey,
											   int count) {
		return getRandomZSetMembers(zSetKey, excludeSetKey, count, Integer.class);
	}

	/**
	 * 简化版Long类型获取
	 */
	public List<Long> getRandomZSetLongs(String zSetKey,
										 String excludeSetKey,
										 int count) {
		return getRandomZSetMembers(zSetKey, excludeSetKey, count, Long.class);
	}
}