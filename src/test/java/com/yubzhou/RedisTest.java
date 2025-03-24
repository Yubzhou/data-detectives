package com.yubzhou;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubzhou.common.UserRole;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.model.po.User;
import com.yubzhou.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

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

}
