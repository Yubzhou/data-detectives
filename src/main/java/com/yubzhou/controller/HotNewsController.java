package com.yubzhou.controller;

import com.yubzhou.util.HotNewsUtil;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.common.Result;
import com.yubzhou.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/news")
public class HotNewsController {
	private final RedisUtil redisUtil;

	@Autowired
	public HotNewsController(RedisUtil redisUtil) {
		this.redisUtil = redisUtil;
	}

	// 查询1小时实时热点
	@GetMapping("/hot/1h")
	public Result<?> get1hHot() {
		String hourKey = RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getCurrentHour();
		return Result.success(getHotNews(hourKey, 10));
	}

	// 查询24小时缓存热点
	@GetMapping("/hot/24h")
	public Result<String> get24hHot() {
		String json = getCachedHotArticles(RedisConstant.HOT_NEWS_24HOUR_CACHE_TOP10);
		return Result.success(json);
	}

	// 查询7天缓存热点
	@GetMapping("/hot/7d")
	public Result<String> get7dHot() {
		String json = getCachedHotArticles(RedisConstant.HOT_NEWS_WEEK_CACHE_TOP10);
		return Result.success(json);
	}

	private List<Map<Object, Object>> getHotNews(String key, int limit) {
		Set<ZSetOperations.TypedTuple<Object>> tuples = redisUtil.zRangeWithScores(key, 0, limit - 1);
		return tuples.stream().map(t -> {
			long newsId = (long) t.getValue();
			return redisUtil.hmget(RedisConstant.NEWS_METRICS_PREFIX + newsId);
		}).toList();
	}

	private String getCachedHotArticles(String cacheKey) {
		String json = (String) redisUtil.get(cacheKey);
		if (json == null) return "[]";
		return json;
	}
}