package com.yubzhou.scheduler;

import com.yubzhou.common.RedisConstant;
import com.yubzhou.consumer.HotNewsCacheService;
import com.yubzhou.util.HotNewsUtil;
import com.yubzhou.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

// 刷新热点新闻缓存定时任务
@Component
@Slf4j
public class RefreshHotNewsCacheScheduler {

	private final RedisUtil redisUtil;
	private final HotNewsCacheService hotNewsCacheService;

	@Autowired
	public RefreshHotNewsCacheScheduler(RedisUtil redisUtil, HotNewsCacheService hotNewsCacheService) {
		this.redisUtil = redisUtil;
		this.hotNewsCacheService = hotNewsCacheService;
	}

	// Yubzhou TODO 2025/4/17 12:35; 添加了新的新闻要手动触发更新元数据缓存（如果有添加新的新闻接口的话）
	// 刷新新闻元数据缓存
	@Scheduled(cron = "0 0 0 * * ?") // 每天0点整执行一次
	public void loadCacheNewsMeta() {
		hotNewsCacheService.loadCacheNewsMeta();
	}

	// 每天凌晨合并前一天的小时数据到天级Key
	@Scheduled(cron = "30 0 0 * * ?") // 每天 00:00:30 执行一次
	public void dailyMerge() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		String dayKey = RedisConstant.HOT_NEWS_DAY_PREFIX + HotNewsUtil.getDay(yesterday);

		// 获取前一天的小时Key
		List<String> hourKeys = hotNewsCacheService.getYesterdayHourKeys();

		redisUtil.zUnionAndStore(dayKey, hourKeys, dayKey);
		redisUtil.expire(dayKey, 8, TimeUnit.DAYS); // 保留8天

		log.info("前一天小时数据合并到天级Key完成");
	}

	// 刷新1小时热点缓存
	@Scheduled(fixedDelay = 180_000, initialDelay = 120_000)
	// 启动后延迟2分钟，之后每3分钟执行一次。fixedDelay:：每次执行间隔时间（以上次执行结束时间为基准），initialDelay: 任务首次执行前的初始延迟
	public void refresh1hCache() {
		String currentHourKey = RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getCurrentHour();
		boolean force = redisUtil.zSize(currentHourKey) < 10L; // 如果当前小时窗口缓存不足10个，则强制更新
		hotNewsCacheService.refresh1hCache(force);
	}

	// 刷新24小时热点缓存
	//@Scheduled(cron = "0 5/10 * * * ?") // 在第5分钟的时候刷新，以后每10分钟刷新一次
	@Scheduled(fixedDelay = 600_000, initialDelay = 300_000)
	// 启动后延迟5分钟，之后每10分钟执行一次。fixedDelay: 每次执行间隔时间（以上次执行结束时间为基准），initialDelay: 任务首次执行前的初始延迟
	public void refresh24hCache() {
		hotNewsCacheService.refresh24hCache(false);
	}

	// 刷新7天热点缓存
	//@Scheduled(cron = "0 10/30 * * * ?") // 在第10分钟的时候刷新，以后每30分钟刷新一次
	@Scheduled(fixedDelay = 1800_000, initialDelay = 600_000)
	// 启动后延迟10分钟，之后每30分钟执行一次。fixedDelay: 每次执行间隔时间（以上次执行结束时间为基准），initialDelay: 任务首次执行前的初始延迟
	public void refresh7dCache() {
		hotNewsCacheService.refresh7dCache(false);
	}
}
