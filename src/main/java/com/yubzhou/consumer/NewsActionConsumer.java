// package com.yubzhou.consumer;
//
// import com.yubzhou.common.KafkaConstant;
// import com.yubzhou.common.RedisConstant;
// import com.yubzhou.common.UserActionEvent;
// import com.yubzhou.common.UserActionEvent.ActionType;
// import com.yubzhou.model.po.News;
// import com.yubzhou.service.NewsService;
// import com.yubzhou.util.RedisUtil;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.redis.core.Cursor;
// import org.springframework.data.redis.core.RedisCallback;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.core.ScanOptions;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
//
// import java.nio.charset.StandardCharsets;
// import java.util.*;
// import java.util.stream.Collectors;
// import java.util.stream.IntStream;
//
// @Service
// @Slf4j
// public class NewsActionConsumer {
//
// 	private final RedisUtil redisUtil;
// 	private final RedisTemplate<String, Object> redisTemplate;
// 	private final NewsService newsService;
//
// 	@Autowired
// 	public NewsActionConsumer(RedisUtil redisUtil, RedisTemplate<String, Object> redisTemplate, NewsService newsService) {
// 		this.redisUtil = redisUtil;
// 		this.redisTemplate = redisTemplate;
// 		this.newsService = newsService;
// 	}
//
// 	private static final Random random = new Random();
//
// 	@KafkaListener(topics = KafkaConstant.USER_ACTION_TOPIC, groupId = KafkaConstant.USER_ACTION_GROUP_ID)
// 	public void processEvent(UserActionEvent event) {
// 		// 1. 更新新闻指标（浏览量、评论量、收藏量等）
// 		String metricsKey = RedisConstant.NEWS_METRICS_PREFIX + event.getNewsId();
// 		redisUtil.hincr(metricsKey, event.getAction().getField(), 1);
//
// 		// 2. 更新当前小时热度
// 		double scoreDelta = getScoreValue(event.getAction());
// 		String currentHourKey = getCurrentHourKey();
// 		redisUtil.zIncrementScore(currentHourKey, event.getNewsId(), scoreDelta);
//
// 		// 3. 设置Hash键过期时间（基础12h + 随机0-12h）
// 		redisUtil.expire(metricsKey, 43200 + random.nextInt(43200));
// 	}
//
// 	private double getScoreValue(UserActionEvent.ActionType action) {
// 		return action.getWeight();
// 	}
//
// 	private String getCurrentHourKey() {
// 		long hourWindow = System.currentTimeMillis() / 3600_1000;
// 		String currentHourKey = RedisConstant.HOT_NEWS_HOUR_PREFIX + hourWindow;
// 		if (!redisUtil.hasKey(currentHourKey)) {
// 			// 如果键不存在，则创建一个空的ZSet
// 			redisUtil.zAdd(currentHourKey, "dummyValue", -1);
// 			// 设置过期时间
// 			redisUtil.expire(currentHourKey, 7 * 24 * 3600);// 设置一周过期
// 		}
// 		return currentHourKey;
// 	}
//
// 	// 10分钟执行一次，合并24小时热点新闻
// 	@Scheduled(fixedRate = 600_000)
// 	public void mergeDailyHot() {
// 		// 获取当前小时窗口
// 		long currentHour = System.currentTimeMillis() / 3600_1000;
// 		// 获取前23个1小时窗口键（1天）
// 		List<String> hourKeys = IntStream.range(1, 24) // 其范围是[1,24)
// 				.mapToObj(i -> RedisConstant.HOT_NEWS_HOUR_PREFIX + (currentHour - i))
// 				.toList();
//
// 		// 使用ZUNIONSTORE合并到当日键
// 		redisUtil.zUnionAndStore(
// 				RedisConstant.HOT_NEWS_HOUR_PREFIX + currentHour, // 当前小时键
// 				hourKeys, // 前23个小时键
// 				RedisConstant.HOT_NEWS_DAY + ":temp" // 临时键
// 		);
// 	}
//
// 	// 30分钟执行一次，合并7天的热点新闻
// 	@Scheduled(fixedRate = 1800_000)
// 	public void mergeWeeklyHot() {
// 		// 获取当前小时窗口
// 		long currentHour = System.currentTimeMillis() / 3600_1000;
// 		// 获取167个历史小时键（7天）
// 		List<String> hourKeys = IntStream.range(0, 168) // 其范围是[0,168)
// 				.mapToObj(i -> RedisConstant.HOT_NEWS_HOUR_PREFIX + (currentHour - i))
// 				.filter(redisUtil::hasKey)
// 				.toList();
//
// 		// 合并到本周键
// 		redisUtil.zUnionAndStore(
// 				RedisConstant.HOT_NEWS_HOUR_PREFIX + currentHour, // 当前小时键
// 				hourKeys, // 前167个小时键
// 				RedisConstant.HOT_NEWS_WEEK + ":temp" // 临时键
// 		);
// 	}
//
// 	// @Scheduled(fixedRate = 300_000)
// 	// public void syncToMySQL() {
// 	// 	// 扫描所有news_metrics:* 键
// 	// 	Set<String> keys = redisUtil.keys(RedisConstant.NEWS_METRICS_PREFIX + "*");
// 	//
// 	// 	List<News> newsList = keys.stream()
// 	// 			// 将其转为新闻ID
// 	// 			.map(key -> key.replace(RedisConstant.NEWS_METRICS_PREFIX, ""))
// 	// 			.filter(key -> !key.isBlank())
// 	// 			.map(key -> News.fromRedisHash(key, redisUtil.hmget(key))) // 获取键值对
// 	// 			.toList();// 转为List
// 	//
// 	// 	newsService.updateBatchById(newsList, 500); // 批量更新到数据库
// 	// }
//
// 	// 每隔30分钟执行一次，同步到MySQL
// 	// 优化：使用SCAN命令获取所有匹配的键，并批量获取哈希数据，减少网络IO
// 	@Scheduled(fixedRate = 1800_000)
// 	public void syncToMySQL() {
// 		// 使用SCAN命令安全地获取所有匹配的键
// 		Set<String> keys = scanKeys(RedisConstant.NEWS_METRICS_PREFIX + "*");
//
// 		// 获取前缀长度，避免每次计算
// 		int prefixLength = RedisConstant.NEWS_METRICS_PREFIX.length();
//
// 		// 提取键和新闻ID对，并过滤无效ID
// 		List<Map.Entry<String, String>> keyIdPairs = keys.stream()
// 				.map(key -> new AbstractMap.SimpleEntry<>(key, key.substring(prefixLength)))
// 				.filter(entry -> !entry.getValue().isBlank())
// 				.collect(Collectors.toList());
//
// 		if (keyIdPairs.isEmpty()) {
// 			return;
// 		}
//
// 		// 分离键和ID列表以保持顺序一致
// 		List<String> redisKeys = keyIdPairs.stream().map(Map.Entry::getKey).toList();
// 		List<String> newsIds = keyIdPairs.stream().map(Map.Entry::getValue).toList();
//
// 		// 通过Pipeline批量获取所有哈希数据
// 		List<Object> hashDataList = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
// 			redisKeys.forEach(key -> connection.hashCommands().hGetAll(key.getBytes(StandardCharsets.UTF_8)));
// 			return null;
// 		});
//
// 		// 转换哈希数据到News对象列表
// 		List<News> newsList = new ArrayList<>(hashDataList.size());
// 		for (int i = 0; i < hashDataList.size(); i++) {
// 			String newsId = newsIds.get(i);
// 			Map<Object, Object> hashData = (Map<Object, Object>) hashDataList.get(i);
// 			try {
// 				News news = convertHashToNews(newsId, hashData);
// 				newsList.add(news);
// 			} catch (Exception e) {
// 				log.error("Failed to convert hash data for newsId: {}", newsId, e);
// 			}
// 		}
//
// 		// 批量更新到数据库
// 		newsService.updateBatchById(newsList, 500);
// 	}
//
// 	// private Set<String> scanKeys(String pattern) {
// 	// 	return redisTemplate.execute((RedisCallback<Set<String>>) conn -> {
// 	// 		Set<String> keys = new HashSet<>();
// 	// 		ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
// 	// 		Cursor<byte[]> cursor = conn.scan(options);
// 	// 		while (cursor.hasNext()) {
// 	// 			keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
// 	// 		}
// 	// 		return keys;
// 	// 	});
// 	// }
//
// 	private Set<String> scanKeys(String pattern) {
// 		return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
// 			final int scanBatchSize = 1000; // 根据集群规模调整
// 			final Set<String> keys = new LinkedHashSet<>(1024);
//
// 			// 构建扫描选项
// 			ScanOptions options = ScanOptions.scanOptions().match(pattern).count(scanBatchSize).build();
//
// 			try (Cursor<byte[]> cursor = connection.scan(options)) {
// 				while (cursor.hasNext()) {
// 					byte[] keyBytes = cursor.next();
// 					if (keyBytes != null && keyBytes.length > 0) {
// 						keys.add(new String(keyBytes, StandardCharsets.UTF_8));
// 					}
// 				}
// 			}
// 			return keys;
// 		});
// 	}
//
//
// 	private News convertHashToNews(String newsId, Map<Object, Object> hashData) {
// 		Map<String, String> dataMap = new HashMap<>();
// 		for (Map.Entry<Object, Object> entry : hashData.entrySet()) {
// 			String field = new String((byte[]) entry.getKey(), StandardCharsets.UTF_8);
// 			String value = (entry.getValue() == null) ?
// 					"" : new String((byte[]) entry.getValue(), StandardCharsets.UTF_8);
// 			dataMap.put(field, value);
// 		}
//
// 		News news = new News();
// 		news.setId(Long.parseLong(newsId));
// 		// 假设使用合理默认值处理可能的空字段
// 		news.setViews(Integer.parseInt(dataMap.getOrDefault(ActionType.VIEW.getField(), "0")));
// 		news.setSupports(Integer.parseInt(dataMap.getOrDefault(ActionType.SUPPORT.getField(), "0")));
// 		news.setOpposes(Integer.parseInt(dataMap.getOrDefault(ActionType.OPPOSE.getField(), "0")));
// 		news.setComments(Integer.parseInt(dataMap.getOrDefault(ActionType.COMMENT.getField(), "0")));
// 		news.setFavorites(Integer.parseInt(dataMap.getOrDefault(ActionType.FAVORITE.getField(), "0")));
// 		return news;
// 	}
//
// }
