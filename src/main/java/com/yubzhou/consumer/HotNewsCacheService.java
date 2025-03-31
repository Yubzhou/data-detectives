package com.yubzhou.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.util.HotNewsUtil;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


@Service
public class HotNewsCacheService {

	private final RedisUtil redisUtil;
	private final ObjectMapper mapper;

	@Autowired
	public HotNewsCacheService(RedisUtil redisUtil, ObjectMapper mapper) {
		this.redisUtil = redisUtil;
		this.mapper = mapper;
	}


	// 每天凌晨合并前一天的小时数据到天级Key
	@Scheduled(cron = "30 0 0 * * ?") // 每天 00:00:30 执行一次
	public void dailyMerge() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		String dayKey = RedisConstant.HOT_NEWS_DAY_PREFIX + HotNewsUtil.getDay(yesterday);

		// 获取前一天的小时Key
		List<String> hourKeys = getYesterdayHourKeys();

		redisUtil.zUnionAndStore(dayKey, hourKeys, dayKey);
		redisUtil.expire(dayKey, 8, TimeUnit.DAYS); // 保留8天
	}

	// 刷新24小时热点缓存
	//@Scheduled(cron = "0 5/10 * * * ?") // 在第5分钟的时候刷新，以后每10分钟刷新一次
	@Scheduled(fixedDelay = 600_000, initialDelay = 300_000)
	// 启动后延迟5分钟，之后每10分钟执行一次。fixedDelay: 每次执行间隔时间（以上次执行结束时间为基准），initialDelay: 任务首次执行前的初始延迟
	public void refresh24hCache() {
		// 获取当前小时Key
		String currentHourKey = RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getCurrentHour();
		// 获取最近24小时Key（当前小时不算）
		List<String> hourKeys = getRecentHourKeys(24);
		String mergedKey = RedisConstant.HOT_NEWS_24HOUR_MERGED_PREFIX + HotNewsUtil.getCurrentDay();

		// 合并前一天的数据和当前小时数据到临时Key
		redisUtil.del(mergedKey); // 合并前先清空临时Key
		redisUtil.zUnionAndStore(currentHourKey, hourKeys, mergedKey);
		cacheTop10(mergedKey, RedisConstant.HOT_NEWS_24HOUR_CACHE_TOP10, 60); // 缓存60分钟
		redisUtil.expire(mergedKey, 1, TimeUnit.DAYS); // 保留1天，设置过期时间防止内存泄漏
	}

	// 刷新7天热点缓存
	//@Scheduled(cron = "0 10/30 * * * ?") // 在第10分钟的时候刷新，以后每30分钟刷新一次
	@Scheduled(fixedDelay = 1800_000, initialDelay = 600_000)
	// 启动后延迟10分钟，之后每30分钟执行一次。fixedDelay: 每次执行间隔时间（以上次执行结束时间为基准），initialDelay: 任务首次执行前的初始延迟
	public void refresh7dCache() {
		// 获取当前24小时Key（当前暂存的，即HOT_NEWS_24HOUR_MERGED）
		String current24hKey = RedisConstant.HOT_NEWS_24HOUR_MERGED_PREFIX + HotNewsUtil.getCurrentDay();
		// 获取最近7天Key（今天不算）
		List<String> dayKeys = getRecentDayKeys(7);
		String mergedKey = RedisConstant.HOT_NEWS_WEEK;

		// 合并前7天的数据和当前24小时数据到最近7天热点Key
		redisUtil.del(mergedKey); // 合并前先清空临时Key
		redisUtil.zUnionAndStore(current24hKey, dayKeys, mergedKey);
		cacheTop10(mergedKey, RedisConstant.HOT_NEWS_WEEK_CACHE_TOP10, 120); // 缓存120分钟
		redisUtil.expire(mergedKey, 8, TimeUnit.DAYS); // 保留8天，设置过期时间防止内存泄漏
	}

	private void cacheTop10(String sourceKey, String cacheKey, int expireMinutes) {
		Set<ZSetOperations.TypedTuple<Object>> tuples = redisUtil.zReverseRangeWithScores(sourceKey, 0, 9);

		List<Map<Map.Entry<String, Long>, Map.Entry<String, Double>>> top10 = tuples.stream()
				.map(t -> Map.of(Map.entry("newsId", (Long) t.getValue()), Map.entry("hotness", t.getScore())))
				.toList();
		String top10Json;
		try {
			top10Json = mapper.writeValueAsString(top10);
		} catch (JsonProcessingException e) {
			throw new BusinessException(ReturnCode.RC500.getCode(), "系统内部错误：序列化热点数据失败");
		}
		redisUtil.set(cacheKey, top10Json, expireMinutes, TimeUnit.MINUTES);
	}

	// 获取到前一整天的小时Key
	private List<String> getYesterdayHourKeys() {
		return IntStream.range(0, 24) // 其值范围为[0, 24)
				.mapToObj(i -> RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getHour
						(LocalDate.now().minusDays(1).atStartOfDay().plusHours(i)))
				.toList();
	}

	// 获取最近n小时的Key，不包括当前小时（即获取前n小时的Key）
	private List<String> getRecentHourKeys(int hours) {
		return IntStream.range(1, hours + 1)// 其值范围为[1, hours]
				.mapToObj(i -> RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getHour(LocalDateTime.now().minusHours(i)))
				.toList();
	}

	// 获取最近n天的Key，不包括今天（即获取前n天的Key）
	private List<String> getRecentDayKeys(int days) {
		return IntStream.range(1, days + 1)// 其值范围为[1, days]
				.mapToObj(i -> RedisConstant.HOT_NEWS_DAY_PREFIX + HotNewsUtil.getDay(LocalDate.now().minusDays(i)))
				.toList();
	}
}