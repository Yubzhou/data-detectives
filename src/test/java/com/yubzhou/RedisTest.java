package com.yubzhou;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.common.UserActionEvent;
import com.yubzhou.common.UserRole;
import com.yubzhou.consumer.HotNewsService;
import com.yubzhou.model.po.News;
import com.yubzhou.model.po.User;
import com.yubzhou.model.pojo.UserDetectionStats;
import com.yubzhou.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
public class RedisTest {
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private HotNewsService hotNewsService;

	@Test
	public void testGetReturnValue() {
		Object value = redisTemplate.opsForValue().get("key");
		System.out.println(value);
	}

	@Test
	public void test01() {
		String key_prefix = "user:id:";
		String key = key_prefix + "2";
		User user1 = new User();
		user1.setPhone("+86-13812345678");
		user1.setRole(UserRole.USER);
		redisTemplate.opsForValue().set(key, user1);
		Object value = redisTemplate.opsForValue().get(key);
		log.info("value: {}; type: {}", value, value.getClass());


		// Object obj =  redisTemplate.opsForValue().get(key);
		// User user2 = mapper.convertValue(obj, User.class);
		// log.info("user: {}", user2);
	}

	@Test
	public void test02() {
		String key_prefix = "user:id:";
		String key = key_prefix + "2";

		List<String> list = new ArrayList<>();
		list.add("apple");
		list.add("banana");
		list.add("orange");

		redisTemplate.opsForValue().set(key, list);
		Object value = redisTemplate.opsForValue().get(key);
		log.info("value: {}; type: {}", value, value.getClass());
	}

	@Test
	public void test03() {
		String key_prefix = "user:id:";
		String key = key_prefix + "2";

		Map<String, Object> map = new HashMap<>();
		map.put("id", 2L);
		map.put("username", "xiaoming");
		map.put("password", "123456");
		map.put("role", UserRole.ADMIN);
		map.put("createdAt", LocalDateTime.now());
		map.put("updatedAt", LocalDateTime.now());

		redisTemplate.opsForValue().set(key, map);
		Object value = redisTemplate.opsForValue().get(key);
		log.info("value: {}; type: {}", value, value.getClass());
	}

	@Test
	public void test04() throws JsonProcessingException {
		String key_prefix = "user:id:";
		String key = key_prefix + "2";

		Map<String, Object> map = new HashMap<>();
		map.put("id", 2L);
		map.put("username", "xiaoming");
		map.put("mail", "example@example.com");
		map.put("password", "123456");
		map.put("role", UserRole.ADMIN);
		map.put("createdAt", LocalDateTime.now());
		map.put("updatedAt", LocalDateTime.now());

		String json = mapper.writeValueAsString(map);

		stringRedisTemplate.opsForValue().set(key, json);
		Object value = stringRedisTemplate.opsForValue().get(key);

		User user = mapper.readValue(value.toString(), User.class);

		log.info("value: {}; type: {}", value, value.getClass());
		log.info("user: {}", user);
	}

	@Test
	public void test05() {
		String key_prefix = "user:id:";
		String key = key_prefix + "2";

		int age = 25;
		redisTemplate.opsForValue().set(key, age);
		Object value = redisTemplate.opsForValue().get(key);
		log.info("value: {}; type: {}", value, value.getClass());
	}

	@Test
	public void test06() {
		String key_prefix = "user:id:";
		String key = key_prefix + "2";

		int age = 25;
		stringRedisTemplate.opsForValue().set(key, String.valueOf(age));
		Object value = stringRedisTemplate.opsForValue().get(key);
		log.info("value: {}; type: {}", value, value.getClass());
	}

	@Test
	public void test07() throws JsonProcessingException {
		String key_prefix = "user:id:";
		String key = key_prefix + "2";

		Map<String, Object> userMap = new HashMap<>();
		userMap.put("id", 2L);
		userMap.put("username", "xiaoming");
		userMap.put("mail", "example@example.com");
		userMap.put("password", "123456");
		userMap.put("role", UserRole.ADMIN);
		userMap.put("createdAt", LocalDateTime.now());
		userMap.put("updatedAt", LocalDateTime.now());

		List<Map<String, Object>> list = new ArrayList<>();
		list.add(userMap);
		list.add(userMap);

		Map<String, Object> map = Map.of("users", list);


		redisTemplate.opsForValue().set(key, map);
		Object value = redisTemplate.opsForValue().get(key);

		long start = System.currentTimeMillis();
		Map resultMap = mapper.convertValue(value, Map.class);
		long end = System.currentTimeMillis();

		log.info("value: {}; type: {}", value, value.getClass());

		log.info("resultMap: {}; type: {}", resultMap, resultMap.getClass());
		log.info("convertValue time: {} ms", end - start);
	}

	@Test
	public void test08() {
		String key_prefix = "user:id:";
		String key = key_prefix + "2";

		// 测试基本数据类型
		int i = 10;
		redisTemplate.opsForValue().set(key, i);
		Object value = redisTemplate.opsForValue().get(key);
		log.info("value: {}; type: {}", value, value.getClass());

		double d = 3.1415926;
		redisTemplate.opsForValue().set(key, d);
		value = redisTemplate.opsForValue().get(key);
		log.info("value: {}; type: {}", value, value.getClass());

		boolean b = true;
		redisTemplate.opsForValue().set(key, b);
		value = redisTemplate.opsForValue().get(key);
		log.info("value: {}; type: {}", value, value.getClass());

		String s = "hello world";
		redisTemplate.opsForValue().set(key, s);
		value = redisTemplate.opsForValue().get(key);
		log.info("value: {}; type: {}", value, value.getClass());
	}

	@Test
	public void test09() {
		// redis的key不可以为null
		// redisTemplate.opsForValue().set(null, "hhhhh");
		// Object obj = redisTemplate.opsForValue().get(null);
		// log.info("obj: {}", obj);

		long start = System.currentTimeMillis();
		Object o = mapper.convertValue(null, Object.class);
		long end = System.currentTimeMillis();
		log.info("o: {}; 耗时: {} ms", o, end - start);
	}

	@Test
	public void test10() {
		// 测试RedisUtil工具类
		// 基本数据类型
		redisUtil.set("int", 10);
		Object value = redisUtil.get("int");
		log.info("value: {}; type: {}", value, value.getClass());

		redisUtil.set("double", 3.1415926);
		value = redisUtil.get("double");
		log.info("value: {}; type: {}", value, value.getClass());

		redisUtil.set("boolean", true);
		value = redisUtil.get("boolean");
		log.info("value: {}; type: {}", value, value.getClass());

		redisUtil.set("string", "hello world");
		value = redisUtil.get("string");
		log.info("value: {}; type: {}", value, value.getClass());

		// 对象类型
		User user = new User();
		user.setId(1L);
		user.setPhone("+86-13812345678");
		user.setPassword("123456");
		user.setRole(UserRole.USER);
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());

		redisUtil.set("user", user);
		value = redisUtil.get("user", User.class);
		log.info("value: {}; type: {}", value, value.getClass());

		// 集合类型
		List<User> userList = new ArrayList<>();
		userList.add(user);
		userList.add(user);
		redisUtil.set("userList", userList);
		value = redisUtil.get("userList");
		log.info("value: {}; type: {}", value, value.getClass());

		// Map类型
		Map<String, Object> userMap = new HashMap<>();
		userMap.put("id", 1L);
		userMap.put("username", "小明");
		userMap.put("password", "123456");
		userMap.put("role", UserRole.USER);
		userMap.put("createdAt", LocalDateTime.now());
		userMap.put("updatedAt", LocalDateTime.now());

		redisUtil.set("userMap", userMap);
		value = redisUtil.get("userMap");
		log.info("value: {}; type: {}", value, value.getClass());
	}

	@Test
	public void test11() {
		int num = 10;
		redisUtil.incr("num1", num);
		Object value = redisUtil.get("num1");
		log.info("value: {}; type: {}", value, value.getClass());

		redisUtil.decr("num2", num);
		value = redisUtil.get("num2");
		log.info("value: {}; type: {}", value, value.getClass());
	}

	@Test
	public void testUserJsonToObject() {
		User user1 = new User();
		user1.setId(1L);
		user1.setPhone("+86-13812345678");
		user1.setPassword("123456");
		user1.setRole(UserRole.USER);
		user1.setCreatedAt(LocalDateTime.now());
		user1.setUpdatedAt(LocalDateTime.now());

		String userJson = "";
		try {
			userJson = mapper.writeValueAsString(user1);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException: ", e);
		}

		log.info("userJson: {}; type: {}", userJson, userJson.getClass());

		User user2 = null;
		try {
			Object userObj = mapper.readValue(userJson, Object.class);
			log.info("user: {}; type: {}", userObj, userObj.getClass());
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException: ", e);
		}
	}

	@Test
	public void testLocalDateTime() {
		final String GLOBAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(GLOBAL_DATE_TIME_FORMAT);
		String formattedDateTime = now.format(formatter);
		log.info("formattedDateTime: {}", formattedDateTime);
	}

	@Test
	public void testRedisUtil() throws Exception {
		// 测试 redis String 类型（存储各种类型的数据）
		redisUtil.set("string", "hello world");
		Object value = redisUtil.get("string");
		log.info("value: {}; type: {}", value, value.getClass());

		// 基本数据类型
		redisUtil.set("int", 10);
		value = redisUtil.get("int", Integer.class);
		log.info("value: {}; type: {}", value, value.getClass());

		redisUtil.set("double", 3.1415926);
		value = redisUtil.get("double", Double.class);
		log.info("value: {}; type: {}", value, value.getClass());

		redisUtil.set("boolean", true);
		value = redisUtil.get("boolean", Boolean.class);
		log.info("value: {}; type: {}", value, value.getClass());

		// 对象类型
		User user = new User();
		user.setId(1L);
		user.setPhone("+86-13812345678");
		user.setPassword("123456");
		user.setRole(UserRole.USER);
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());

		redisUtil.set("user", user);
		value = redisUtil.get("user", User.class);
		log.info("value: {}; type: {}", value, value.getClass());

		// 集合类型
		List<User> userList = new ArrayList<>();
		userList.add(user);
		userList.add(user);
		redisUtil.set("userList", userList);
		value = redisUtil.get("userList", List.class);
		log.info("value: {}; type: {}", value, value.getClass());

		// Map类型
		Map<String, Object> userMap = new HashMap<>();
		userMap.put("id", 1L);
		userMap.put("username", "小明");
		userMap.put("password", "123456");
		userMap.put("role", UserRole.USER);
		userMap.put("createdAt", LocalDateTime.now());
		userMap.put("updatedAt", LocalDateTime.now());

		redisUtil.set("userMap", userMap);
		value = redisUtil.get("userMap", Map.class);
		log.info("value: {}; type: {}", value, value.getClass());

		// 测试 redis Hash 类型（存储键值对）
		redisUtil.hset("hash", "key1", 100);
		redisUtil.hset("hash", "key2", 200);
		value = redisUtil.hmget("hash");
		log.info("value: {}; type: {}", value, value.getClass());
		log.info("value: {}; type: {}", ((Map<?, ?>) value).get("key1"), ((Map<?, ?>) value).get("key1").getClass());

		// 测试 redis List 类型（存储列表数据）
		redisUtil.lSet("list", "value1");
		redisUtil.lSet("list", "value2");
		value = redisUtil.lGet("list", 0, -1);
		log.info("value: {}; type: {}", value, value.getClass());

		// 测试 redis Set 类型（存储集合数据）
		redisUtil.sSet("set", "value1");
		redisUtil.sSet("set", "value2");
		value = redisUtil.sGet("set");
		log.info("value: {}; type: {}", value, value.getClass());

		// 测试 redis Sorted Set 类型（存储有序集合数据）
		redisUtil.zAdd("sortedSet", "value1", 10);
		redisUtil.zAdd("sortedSet", "value2", 20);
		value = redisUtil.zRangeWithScores("sortedSet", 0, -1);
		log.info("value: {}; type: {}", value, value.getClass());
	}

	@Test
	public void test12() throws Exception {
		redisUtil.sSet("set", "value1", "value2");
		long num1 = redisUtil.sRemove("set", "value1");
		long num2 = redisUtil.sRemove("set", "value3");
		log.info("num1: {}, num2: {}", num1, num2);
	}

	@Test
	public void test13() throws Exception {
		redisUtil.set("key", "value");
		redisUtil.set("key1", 10);

		// redisUtil.set("key", "value");

		Object value = redisUtil.get("key");
		log.info("value: {}; type: {}", value, value.getClass());
		value = redisUtil.get("key1");
		log.info("value: {}; type: {}", value, value.getClass());

		User user = new User();
		user.setId(1L);
		user.setPhone("13812345678");
		user.setPassword("123456");

		redisUtil.set("user", user);
		value = redisUtil.get("user");
		log.info("value: {}; type: {}", value, value.getClass());
		value = redisUtil.get("user", User.class);
		log.info("value: {}; type: {}", value, value.getClass());
	}

	@Test
	public void test14() throws Exception {
		List<News> newsList = List.of(
				new News(70L),
				new News(71L),
				new News(72L),
				new News(73L),
				new News(74L)
		);

		// Long[] userIds = newsList.stream().map(News::getId).toArray(Long[]::new);
		Object[] userIds = newsList.stream().map(News::getId).toArray();

		String userKeyPrefix = RedisConstant.USER_NEWS_ACTION_PREFIX + 4 + ":";
		String viewsKey = userKeyPrefix + UserActionEvent.ActionType.VIEW.getField();

		Map<Object, Boolean> views = redisUtil.sHasKeys(viewsKey, userIds);

		System.out.println(views);

		for (Map.Entry<Object, Boolean> entry : views.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue() + "; key type: " + entry.getKey().getClass());
		}
	}

	@Test
	public void test15() throws Exception {
		// hash存储field和value都为整数的测试案例
		Map<String, String> map = Map.of("1", "2", "3", "4");

		redisTemplate.opsForHash().putAll("hash", map);

		Map hash = redisTemplate.opsForHash().entries("hash");
		log.info("hash: {}; type: {}", hash, hash.getClass());
		// log.info("hash: {}; type: {}", ((Map<?, ?>) hash).get(1), ((Map<?, ?>) hash).get(1).getClass());
	}

	@Test
	public void test16() throws Exception {
		String key = "not_exist_key";
		String value = (String) redisTemplate.opsForValue().get(key);
		System.out.println(value);
		if (value != null) {
			System.out.println(value.getClass());
		}
	}

	@Test
	public void test17() throws Exception {
		String key = "user:detection:stats:1";

		UserDetectionStats stats = UserDetectionStats.createDefault();
		redisTemplate.opsForValue().set(key, stats);

		Object value = redisUtil.get(key);
		System.out.println(value);
		if (value != null) {
			System.out.println(value.getClass());
		}

		UserDetectionStats stats1 = redisUtil.get(key, UserDetectionStats.class);
		System.out.println(stats1);
		if (stats1 != null) {
			System.out.println(stats1.getClass());
		}
	}


}
