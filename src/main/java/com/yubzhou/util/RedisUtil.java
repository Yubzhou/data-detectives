package com.yubzhou.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class RedisUtil {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper mapper;

	@Autowired
	public RedisUtil(RedisTemplate<String, Object> redisTemplate, ObjectMapper mapper) {
		this.redisTemplate = redisTemplate;
		this.mapper = mapper;
	}

	// ============================= Common ============================

	/**
	 * 设置键的过期时间
	 *
	 * @param key  键名
	 * @param time 过期时间（秒）
	 * @return 设置成功返回 true，否则返回 false
	 */
	public boolean expire(String key, long time) {
		return expire(key, time, TimeUnit.SECONDS);
	}

	/**
	 * 设置键的过期时间
	 *
	 * @param key  键名
	 * @param time 过期时间
	 * @param unit 时间单位
	 * @return 设置成功返回 true，否则返回 false
	 */
	public boolean expire(String key, long time, TimeUnit unit) {
		try {
			if (time > 0) {
				redisTemplate.expire(key, time, unit);
			}
			return true;
		} catch (Exception e) {
			log.error("Redis expire error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 获取键的剩余过期时间
	 *
	 * @param key 键名
	 * @return 整数：剩余时间（秒）；-1：键存在，但未设置过期时间；-2：键不存在；
	 */
	public long getExpire(String key) {
		return redisTemplate.getExpire(key, TimeUnit.SECONDS);
	}

	/**
	 * 判断键是否存在
	 *
	 * @param key 键名
	 * @return 存在返回 true，否则返回 false
	 */
	public boolean hasKey(String key) {
		try {
			return redisTemplate.hasKey(key);
		} catch (Exception e) {
			log.error("Redis hasKey error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 删除一个或多个键
	 *
	 * @param keys 可变参数，支持删除多个键
	 */
	public void del(String... keys) {
		if (keys != null && keys.length > 0) {
			if (keys.length == 1) {
				redisTemplate.delete(keys[0]);
			} else {
				redisTemplate.delete(Arrays.asList(keys));
			}
		}
	}

	/**
	 * 根据模板获取全部键
	 *
	 * @param pattern 模板
	 * @return 键集合
	 */
	public Set<String> keys(String pattern) {
		return redisTemplate.keys(pattern);
	}

	// ============================= String ============================

	/**
	 * 获取字符串类型的值
	 *
	 * @param key 键名
	 * @return 值，若键不存在返回 null
	 */
	public Object get(String key) {
		return key == null ? null : redisTemplate.opsForValue().get(key);
	}

	/**
	 * 获取字符串类型的值，并反序列化为指定类型对象
	 *
	 * @param key   键名
	 * @param clazz 目标对象类型
	 * @param <T>   泛型类型
	 * @return 反序列化后的对象，反序列化失败或键不存在返回 null
	 */
	public <T> T get(String key, Class<T> clazz) {
		Object obj = get(key);
		if (obj == null) return null;
		try {
			return mapper.convertValue(obj, clazz);
		} catch (Exception e) {
			log.error("Redis get error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 设置字符串类型的值
	 *
	 * @param key   键名
	 * @param value 值对象
	 * @return 设置成功返回 true，否则返回 false
	 */
	public boolean set(String key, Object value) {
		try {
			redisTemplate.opsForValue().set(key, value);
			return true;
		} catch (Exception e) {
			log.error("Redis set error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 设置字符串类型的值并指定过期时间
	 *
	 * @param key   键名
	 * @param value 值对象
	 * @param time  过期时间（秒），需大于 0
	 * @return 设置成功返回 true，否则返回 false
	 */
	public boolean set(String key, Object value, long time) {
		return set(key, value, time, TimeUnit.SECONDS);
	}

	/**
	 * 设置字符串类型的值并指定过期时间
	 *
	 * @param key   键名
	 * @param value 值对象
	 * @param time  过期时间
	 * @param unit  时间单位
	 * @return 设置成功返回 true，否则返回 false
	 */
	public boolean set(String key, Object value, long time, TimeUnit unit) {
		try {
			if (time > 0) {
				redisTemplate.opsForValue().set(key, value, time, unit);
			} else {
				set(key, value);
			}
			return true;
		} catch (Exception e) {
			log.error("Redis set error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 对键的值进行递增操作
	 * 如果键不存在，则创建并设置值为 delta，否则原子递增 delta
	 *
	 * @param key   键名
	 * @param delta 递增步长（必须为正数）
	 * @return 递增后的值
	 * @throws RuntimeException 如果步长为负数
	 */
	public long incr(String key, long delta) {
		if (delta < 0) throw new RuntimeException("Delta must be positive");
		return redisTemplate.opsForValue().increment(key, delta);
	}

	/**
	 * 对键的值进行递减操作
	 * 如果键不存在，则创建并设置值为 delta，否则原子递减 delta
	 *
	 * @param key   键名
	 * @param delta 递减步长（必须为正数）
	 * @return 递减后的值
	 * @throws RuntimeException 如果步长为负数
	 */
	public long decr(String key, long delta) {
		if (delta < 0) throw new RuntimeException("Delta must be positive");
		return redisTemplate.opsForValue().increment(key, -delta);
	}

	// ============================= Hash ============================

	/**
	 * 获取哈希表中指定字段的值
	 *
	 * @param key   键名
	 * @param field 字段名
	 * @return 字段对应的值，字段不存在返回 null
	 */
	public Object hget(String key, String field) {
		return redisTemplate.opsForHash().get(key, field);
	}

	/**
	 * 获取哈希表的所有字段和值
	 *
	 * @param key 键名
	 * @return 哈希表对应的 Map 对象，键不存在返回空 Map
	 */
	public Map<Object, Object> hmget(String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	/**
	 * 批量设置哈希表的字段和值
	 *
	 * @param key 键名
	 * @param map 字段-值映射表
	 * @return 操作成功返回 true，否则返回 false
	 */
	public boolean hmset(String key, Map<String, Object> map) {
		try {
			redisTemplate.opsForHash().putAll(key, map);
			return true;
		} catch (Exception e) {
			log.error("Redis hmset error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 批量设置哈希表的字段和值，并指定过期时间
	 *
	 * @param key  键名
	 * @param map  字段-值映射表
	 * @param time 过期时间（秒）
	 * @return 操作成功返回 true，否则返回 false
	 */
	public boolean hmset(String key, Map<String, Object> map, long time) {
		try {
			redisTemplate.opsForHash().putAll(key, map);
			if (time > 0) expire(key, time);
			return true;
		} catch (Exception e) {
			log.error("Redis hmset error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 设置哈希表单个字段的值
	 *
	 * @param key   键名
	 * @param field 字段名
	 * @param value 值
	 * @return 操作成功返回 true，否则返回 false
	 */
	public boolean hset(String key, String field, Object value) {
		try {
			redisTemplate.opsForHash().put(key, field, value);
			return true;
		} catch (Exception e) {
			log.error("Redis hset error, key: {}, field: {}", key, field, e);
			return false;
		}
	}

	/**
	 * 设置哈希表单个字段的值，并指定过期时间
	 *
	 * @param key   键名
	 * @param field 字段名
	 * @param value 值
	 * @param time  过期时间（秒）
	 * @return 操作成功返回 true，否则返回 false
	 */
	public boolean hset(String key, String field, Object value, long time) {
		try {
			redisTemplate.opsForHash().put(key, field, value);
			if (time > 0) expire(key, time);
			return true;
		} catch (Exception e) {
			log.error("Redis hset error, key: {}, field: {}", key, field, e);
			return false;
		}
	}

	/**
	 * 删除哈希表中一个或多个字段
	 *
	 * @param key    键名
	 * @param fields 可变参数，字段名列表
	 */
	public void hdel(String key, Object... fields) {
		redisTemplate.opsForHash().delete(key, fields);
	}

	/**
	 * 判断哈希表中是否存在指定字段
	 *
	 * @param key   键名
	 * @param field 字段名
	 * @return 存在返回 true，否则返回 false
	 */
	public boolean hHasField(String key, String field) {
		return redisTemplate.opsForHash().hasKey(key, field);
	}

	/**
	 * 对哈希表字段的值进行递增操作
	 *
	 * @param key   键名
	 * @param field 字段名
	 * @param delta 递增步长
	 * @return 递增后的值
	 */
	public double hincr(String key, String field, double delta) {
		return redisTemplate.opsForHash().increment(key, field, delta);
	}

	/**
	 * 对哈希表字段的值进行递减操作
	 *
	 * @param key   键名
	 * @param field 字段名
	 * @param delta 递减步长
	 * @return 递减后的值
	 */
	public double hdecr(String key, String field, double delta) {
		return redisTemplate.opsForHash().increment(key, field, -delta);
	}

	// ============================= Set ============================

	/**
	 * 获取集合中的所有成员
	 *
	 * @param key 键名
	 * @return 成员集合，键不存在返回空集合
	 */
	public Set<Object> sGet(String key) {
		try {
			return redisTemplate.opsForSet().members(key);
		} catch (Exception e) {
			log.error("Redis sGet error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 判断集合中是否包含指定值
	 *
	 * @param key   键名
	 * @param value 值
	 * @return 包含返回 true，否则返回 false
	 */
	public boolean sHasKey(String key, Object value) {
		try {
			return redisTemplate.opsForSet().isMember(key, value);
		} catch (Exception e) {
			log.error("Redis sHasKey error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 判断集合中是否包含指定值
	 *
	 * @param key    键名
	 * @param values 可变参数，值列表
	 * @return 成员集合，键不存在返回空集合
	 */
	public Map<Object, Boolean> sHasKeys(String key, Object... values) {
		try {
			return redisTemplate.opsForSet().isMember(key, values);
		} catch (Exception e) {
			log.error("Redis sHasKeys error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 向集合中添加一个或多个成员
	 *
	 * @param key    键名
	 * @param values 可变参数，成员列表
	 * @return 成功添加的成员数量
	 */
	public long sSet(String key, Object... values) {
		try {
			return redisTemplate.opsForSet().add(key, values);
		} catch (Exception e) {
			log.error("Redis sSet error, key: {}", key, e);
			return 0;
		}
	}

	/**
	 * 向集合中添加成员并设置过期时间
	 *
	 * @param key    键名
	 * @param time   过期时间（秒）
	 * @param values 可变参数，成员列表
	 * @return 成功添加的成员数量
	 */
	public long sSetAndTime(String key, long time, Object... values) {
		try {
			Long count = redisTemplate.opsForSet().add(key, values);
			if (time > 0) expire(key, time);
			return count != null ? count : 0;
		} catch (Exception e) {
			log.error("Redis sSetAndTime error, key: {}", key, e);
			return 0;
		}
	}

	/**
	 * 获取集合的成员总数
	 *
	 * @param key 键名
	 * @return 成员数量，键不存在返回 0
	 */
	public long sSize(String key) {
		try {
			return redisTemplate.opsForSet().size(key);
		} catch (Exception e) {
			log.error("Redis sSize error, key: {}", key, e);
			return 0;
		}
	}

	/**
	 * 移除集合中一个或多个成员
	 *
	 * @param key    键名
	 * @param values 可变参数，成员列表
	 * @return 成功移除的成员数量
	 */
	public long sRemove(String key, Object... values) {
		try {
			return redisTemplate.opsForSet().remove(key, values);
		} catch (Exception e) {
			log.error("Redis setRemove error, key: {}", key, e);
			return 0;
		}
	}

	// ============================= ZSet ============================

	/**
	 * 获取有序集合中指定排名范围的成员（升序）
	 *
	 * @param key   键名
	 * @param start 起始排名（包含）
	 * @param end   结束排名（包含）
	 * @return 成员集合
	 */
	public Set<Object> zRange(String key, long start, long end) {
		try {
			return redisTemplate.opsForZSet().range(key, start, end);
		} catch (Exception e) {
			log.error("Redis zRange error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 获取有序集合中指定排名范围的成员及其分数（升序）
	 *
	 * @param key   键名
	 * @param start 起始排名（包含）
	 * @param end   结束排名（包含）
	 * @return 成员-分数元组集合
	 */
	public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
		try {
			return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
		} catch (Exception e) {
			log.error("Redis zRangeWithScores error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 获取有序集合中指定排名范围的成员（降序）
	 *
	 * @param key   键名
	 * @param start 起始排名（包含）
	 * @param end   结束排名（包含）
	 * @return 成员集合
	 */
	public Set<Object> zReverseRange(String key, long start, long end) {
		try {
			return redisTemplate.opsForZSet().reverseRange(key, start, end);
		} catch (Exception e) {
			log.error("Redis zReverseRange error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 获取有序集合中指定排名范围的成员及其分数（降序）
	 *
	 * @param key   键名
	 * @param start 起始排名（包含）
	 * @param end   结束排名（包含）
	 * @return 成员-分数元组集合
	 */
	public Set<ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
		try {
			return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
		} catch (Exception e) {
			log.error("Redis zReverseRangeWithScores error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 向有序集合中添加一个成员
	 *
	 * @param key   键名
	 * @param value 成员
	 * @param score 分数
	 * @return 添加成功返回 true，否则返回 false
	 */
	public Boolean zAdd(String key, Object value, double score) {
		try {
			return redisTemplate.opsForZSet().add(key, value, score);
		} catch (Exception e) {
			log.error("Redis zAdd error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 批量向有序集合中添加成员-分数元组
	 *
	 * @param key    键名
	 * @param tuples 成员-分数元组集合
	 * @return 成功添加的成员数量
	 */
	public Long zAdd(String key, Set<ZSetOperations.TypedTuple<Object>> tuples) {
		try {
			return redisTemplate.opsForZSet().add(key, tuples);
		} catch (Exception e) {
			log.error("Redis zAdd error, key: {}", key, e);
			return 0L;
		}
	}

	/**
	 * 向有序集合中添加一个成员，并设置过期时间
	 *
	 * @param key   键名
	 * @param value 成员
	 * @param score 分数
	 * @return 添加成功返回 true，否则返回 false
	 */
	public Boolean zAddIfAbsent(String key, Object value, double score) {
		try {
			return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
		} catch (Exception e) {
			log.error("Redis zAddIfAbsent error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 从有序集合中移除一个或多个成员
	 *
	 * @param key    键名
	 * @param values 可变参数，成员列表
	 * @return 成功移除的成员数量
	 */
	public Long zRemove(String key, Object... values) {
		try {
			return redisTemplate.opsForZSet().remove(key, values);
		} catch (Exception e) {
			log.error("Redis zRemove error, key: {}", key, e);
			return 0L;
		}
	}

	/**
	 * 增加有序集合中成员的分数
	 *
	 * @param key   键名
	 * @param value 成员
	 * @param delta 要增加的分数值（可正可负）
	 * @return 增加后的新分数，操作失败返回 null
	 */
	public Double zIncrementScore(String key, Object value, double delta) {
		try {
			return redisTemplate.opsForZSet().incrementScore(key, value, delta);
		} catch (Exception e) {
			log.error("Redis zIncrementScore error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 获取成员在有序集合中的升序排名（从0开始）
	 *
	 * @param key   键名
	 * @param value 成员
	 * @return 成员的排名，若成员不存在返回 null
	 */
	public Long zRank(String key, Object value) {
		try {
			return redisTemplate.opsForZSet().rank(key, value);
		} catch (Exception e) {
			log.error("Redis zRank error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 获取有序集合的成员总数
	 *
	 * @param key 键名
	 * @return 成员数量，键不存在返回 0
	 */
	public Long zSize(String key) {
		try {
			return redisTemplate.opsForZSet().size(key);
		} catch (Exception e) {
			log.error("Redis zSize error, key: {}", key, e);
			return 0L;
		}
	}

	/**
	 * 将key存在的集合与otherKeys指定的集合合并，并存储到destKey指定的集合中
	 *
	 * @param key       键名
	 * @param otherKeys 其他集合的键名列表
	 * @param destKey   目标集合的键名
	 * @return 合并后的集合元素数量
	 */
	public Long zUnionAndStore(String key, List<String> otherKeys, String destKey) {
		try {
			return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
		} catch (Exception e) {
			log.error("Redis zUnionStore error, key: {}", key, e);
			return 0L;
		}
	}


// ============================= List ============================

	/**
	 * 获取列表中指定索引范围的元素
	 *
	 * @param key   键名
	 * @param start 起始索引（包含，从0开始）
	 * @param end   结束索引（包含，-1表示末尾）
	 * @return 元素列表，键不存在或异常时返回 null
	 */
	public List<Object> lGet(String key, long start, long end) {
		try {
			return redisTemplate.opsForList().range(key, start, end);
		} catch (Exception e) {
			log.error("Redis lGet error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 获取列表的长度
	 *
	 * @param key 键名
	 * @return 列表长度，键不存在返回 0
	 */
	public long lSize(String key) {
		try {
			return redisTemplate.opsForList().size(key);
		} catch (Exception e) {
			log.error("Redis lSize error, key: {}", key, e);
			return 0;
		}
	}

	/**
	 * 通过索引获取列表中的元素
	 *
	 * @param key   键名
	 * @param index 索引（从0开始，负数表示从末尾开始）
	 * @return 元素值，索引越界或异常返回 null
	 */
	public Object lGetIndex(String key, long index) {
		try {
			return redisTemplate.opsForList().index(key, index);
		} catch (Exception e) {
			log.error("Redis lGetIndex error, key: {}", key, e);
			return null;
		}
	}

	/**
	 * 向列表尾部添加单个元素
	 *
	 * @param key   键名
	 * @param value 元素值
	 * @return 操作成功返回 true，否则返回 false
	 */
	public boolean lSet(String key, Object value) {
		try {
			redisTemplate.opsForList().rightPush(key, value);
			return true;
		} catch (Exception e) {
			log.error("Redis lSet error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 向列表尾部添加单个元素并设置过期时间
	 *
	 * @param key   键名
	 * @param value 元素值
	 * @param time  过期时间（秒）
	 * @return 操作成功返回 true，否则返回 false
	 */
	public boolean lSet(String key, Object value, long time) {
		try {
			redisTemplate.opsForList().rightPush(key, value);
			if (time > 0) expire(key, time);
			return true;
		} catch (Exception e) {
			log.error("Redis lSet error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 向列表尾部批量添加多个元素
	 *
	 * @param key    键名
	 * @param values 元素列表
	 * @return 操作成功返回 true，否则返回 false
	 */
	public boolean lSetAll(String key, List<Object> values) {
		try {
			redisTemplate.opsForList().rightPushAll(key, values);
			return true;
		} catch (Exception e) {
			log.error("Redis lSetAll error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 向列表尾部批量添加多个元素并设置过期时间
	 *
	 * @param key    键名
	 * @param values 元素列表
	 * @param time   过期时间（秒）
	 * @return 操作成功返回 true，否则返回 false
	 */
	public boolean lSetAll(String key, List<Object> values, long time) {
		try {
			redisTemplate.opsForList().rightPushAll(key, values);
			if (time > 0) expire(key, time);
			return true;
		} catch (Exception e) {
			log.error("Redis lSetAll error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 根据索引更新列表中的元素
	 *
	 * @param key   键名
	 * @param index 索引位置（从0开始）
	 * @param value 新元素值
	 * @return 操作成功返回 true，索引越界或异常返回 false
	 */
	public boolean lUpdateIndex(String key, long index, Object value) {
		try {
			redisTemplate.opsForList().set(key, index, value);
			return true;
		} catch (Exception e) {
			log.error("Redis lUpdateIndex error, key: {}", key, e);
			return false;
		}
	}

	/**
	 * 移除列表中指定数量的元素
	 *
	 * @param key   键名
	 * @param count 移除数量：
	 *              count > 0 : 从头部开始移除前count个匹配项
	 *              count < 0 : 从尾部开始移除前count个匹配项
	 *              count = 0 : 移除所有匹配项
	 * @param value 要移除的元素值
	 * @return 实际移除的元素数量
	 */
	public long lRemove(String key, long count, Object value) {
		try {
			return redisTemplate.opsForList().remove(key, count, value);
		} catch (Exception e) {
			log.error("Redis lRemove error, key: {}", key, e);
			return 0;
		}
	}
}