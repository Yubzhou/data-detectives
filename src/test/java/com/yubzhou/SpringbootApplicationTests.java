package com.yubzhou;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.model.po.News;
import com.yubzhou.properties.AsyncProperties;
import com.yubzhou.properties.FileUploadProperties;
import com.yubzhou.service.NewsService;
import com.yubzhou.service.UserService;
import com.yubzhou.service.impl.NewsLoader;
import com.yubzhou.service.impl.NewsServiceImpl;
import com.yubzhou.util.PathUtil;
import com.yubzhou.util.RedisUtil;
import com.yubzhou.util.SpelEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
class SpringbootApplicationTests {

	@Autowired
	private FileUploadProperties fileUploadProperties;

	@Autowired
	private AsyncProperties asyncProperties;

	@Autowired
	private SpelEvaluator spelEvaluator;

	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private UserService userService;

	@Autowired
	private NewsLoader newsLoader;

	@Autowired
	private NewsServiceImpl newsServiceImpl;

	@Test
	public void test01() throws Exception {
		String imagePath = PathUtil.getExternalPath(fileUploadProperties.getImage().getUploadDir()).toString();
		System.out.println(imagePath);
	}

	@Test
	public void test02() throws Exception {
		System.out.println(asyncProperties.getSse());
		System.out.println(asyncProperties.getUpload());
		System.out.println(asyncProperties.getGlobal());
	}

	@Test
	public void test03() throws Exception {
		String spelExpression = "T(java.lang.Math).PI";

		Double PI = spelEvaluator.evaluate(spelExpression, Double.class);
		System.out.println(PI);
		System.out.println(spelEvaluator.getCacheStats());
	}

	@Test
	public void test04() throws Exception {
		long hourWindow = System.currentTimeMillis() / 3600_1000;
		String currentHourKey = "hotnews:1h:" + hourWindow;
		if (!redisUtil.hasKey(currentHourKey)) {
			// 如果键不存在，则创建一个空的ZSet
			redisUtil.zAdd(currentHourKey, "dummyValue", 0.0);
			// 设置过期时间
			redisUtil.expire(currentHourKey, 7 * 24 * 3600);// 设置一周过期
		}
	}

	@Test
	public void test05() throws Exception {
		Map<Object, Object> metrics = Map.of(
				"views", 1,
				"supports", 2,
				"opposes", 3,
				"comments", 4,
				"favorites", 5,
				"createdAt", 25465475
		);

		// 转换为 News对象
		News news = mapper.convertValue(metrics, News.class);
		log.warn("value：{}, type：{}", news, news.getClass().getName());
	}


	// @Test
	// public void test06() throws Exception {
	// 	List<User> users = List.of(
	// 			new User(4L, "12345678901"),
	// 			new User(6L, "12345678903"),
	// 			new User(7L, "12345678902")
	// 	);
	//
	// 	List<Long> userIds = users.stream().map(User::getId).toList();
	//
	// 	List<User> userList = userService.listByIds(userIds);
	//
	// 	System.out.println(users);
	// 	System.out.println(userIds);
	// 	System.out.println(userList);
	// }

	@Test
	public void testNewsLoader() throws Exception {
		newsLoader.loadNewsData();
	}

	@Test
	public void test07() throws Exception {
		System.out.println(newsServiceImpl.listByIds(List.of(1L, 2L, 3L)));
	}
}
