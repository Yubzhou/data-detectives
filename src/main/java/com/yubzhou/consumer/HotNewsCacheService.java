package com.yubzhou.consumer;

import com.yubzhou.common.HotNewsActionTracker;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.common.UserActionEvent;
import com.yubzhou.model.po.News;
import com.yubzhou.model.vo.HotNews;
import com.yubzhou.model.vo.NewsVo;
import com.yubzhou.service.NewsCategoryRelationService;
import com.yubzhou.service.NewsService;
import com.yubzhou.util.HotNewsUtil;
import com.yubzhou.util.RedisUtil;
import com.yubzhou.util.RedisZSetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;


@Service
@Slf4j
public class HotNewsCacheService {

	private final RedisUtil redisUtil;
	private final RedisZSetUtil redisZSetUtil;
	private final ThreadPoolTaskExecutor globalTaskExecutor;
	private final NewsService newsService;
	private final HotNewsService hotNewsService;
	private final HotNewsActionTracker hotNewsActionTracker;
	private final NewsCategoryRelationService newsCategoryRelationService;

	@Autowired
	public HotNewsCacheService(RedisUtil redisUtil,
							   RedisZSetUtil redisZSetUtil,
							   @Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor,
							   NewsService newsService,
							   HotNewsService hotNewsService,
							   HotNewsActionTracker hotNewsActionTracker,
							   NewsCategoryRelationService newsCategoryRelationService) {
		this.redisUtil = redisUtil;
		this.redisZSetUtil = redisZSetUtil;
		this.globalTaskExecutor = globalTaskExecutor;
		this.newsService = newsService;
		this.hotNewsService = hotNewsService;
		this.hotNewsActionTracker = hotNewsActionTracker;
		this.newsCategoryRelationService = newsCategoryRelationService;
	}

	private static final Random RANDOM = new Random();

	// 使用并发集合
	private final Map<String, Object> NEWS_META = new ConcurrentHashMap<>();
	private final List<HotNews> HOT_NEWS_HOUR_CACHE_TOP10 = new CopyOnWriteArrayList<>();
	private final List<HotNews> HOT_NEWS_24HOUR_CACHE_TOP10 = new CopyOnWriteArrayList<>();
	private final List<HotNews> HOT_NEWS_WEEK_CACHE_TOP10 = new CopyOnWriteArrayList<>();

	// 同步新闻元数据
	public void setMinAndMaxId(NewsService.MinAndMaxId minAndMaxId) {
		synchronized (NEWS_META) {
			NEWS_META.put("minAndMaxId", minAndMaxId);
		}
	}

	// 同步更新1小时缓存
	private void setHourCacheTop10(List<HotNews> top10) {
		synchronized (HOT_NEWS_HOUR_CACHE_TOP10) {
			HOT_NEWS_HOUR_CACHE_TOP10.clear();
			HOT_NEWS_HOUR_CACHE_TOP10.addAll(top10);
		}
	}

	// 同步更新24小时缓存
	private void set24HourCacheTop10(List<HotNews> top10) {
		synchronized (HOT_NEWS_24HOUR_CACHE_TOP10) {
			HOT_NEWS_24HOUR_CACHE_TOP10.clear();
			HOT_NEWS_24HOUR_CACHE_TOP10.addAll(top10);
		}
	}

	// 同步更新7天缓存
	private void setWeekCacheTop10(List<HotNews> top10) {
		synchronized (HOT_NEWS_WEEK_CACHE_TOP10) {
			HOT_NEWS_WEEK_CACHE_TOP10.clear();
			HOT_NEWS_WEEK_CACHE_TOP10.addAll(top10);
		}
	}

	// 获取新闻元数据
	public NewsService.MinAndMaxId getMinAndMaxId() {
		return (NewsService.MinAndMaxId) NEWS_META.get("minAndMaxId");
	}

	// 获取1小时热点缓存
	public List<HotNews> getHourCacheTop10() {
		return HOT_NEWS_HOUR_CACHE_TOP10;
	}

	// 获取24小时热点缓存
	public List<HotNews> get24HourCacheTop10() {
		return HOT_NEWS_24HOUR_CACHE_TOP10;
	}

	// 获取7天热点缓存
	public List<HotNews> getWeekCacheTop10() {
		return HOT_NEWS_WEEK_CACHE_TOP10;
	}

	// Yubzhou TODO 2025/4/17 12:35; 添加了新闻要手动触发更新元数据缓存
	// 刷新新闻元数据缓存
	@Scheduled(cron = "0 0 0 * * ?") // 每天0点整执行一次
	public void loadCacheNewsMeta() {
		NewsService.MinAndMaxId minAndMaxId = newsService.findMinAndMaxId();
		if (minAndMaxId == null) return;
		redisUtil.hmset(RedisConstant.NEWS_META,
				Map.of("minId", minAndMaxId.getMinId(), "maxId", minAndMaxId.getMaxId()));

		setMinAndMaxId(minAndMaxId);

		log.info("刷新新闻元数据缓存完成");
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

		log.info("前一天小时数据合并到天级Key完成");
	}

	// 刷新1小时热点缓存
	@Scheduled(fixedDelay = 180_000, initialDelay = 120_000)
	// 启动后延迟2分钟，之后每3分钟执行一次。fixedDelay:：每次执行间隔时间（以上次执行结束时间为基准），initialDelay: 任务首次执行前的初始延迟
	public void refresh1hCache() {
		String currentHourKey = RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getCurrentHour();
		boolean force = redisUtil.zSize(currentHourKey) < 10L; // 如果当前小时窗口缓存不足10个，则强制更新
		refresh1hCache(force);
	}

	// 刷新24小时热点缓存
	//@Scheduled(cron = "0 5/10 * * * ?") // 在第5分钟的时候刷新，以后每10分钟刷新一次
	@Scheduled(fixedDelay = 600_000, initialDelay = 300_000)
	// 启动后延迟5分钟，之后每10分钟执行一次。fixedDelay: 每次执行间隔时间（以上次执行结束时间为基准），initialDelay: 任务首次执行前的初始延迟
	public void refresh24hCache() {
		refresh24hCache(false);
	}

	// 刷新7天热点缓存
	//@Scheduled(cron = "0 10/30 * * * ?") // 在第10分钟的时候刷新，以后每30分钟刷新一次
	@Scheduled(fixedDelay = 1800_000, initialDelay = 600_000)
	// 启动后延迟10分钟，之后每30分钟执行一次。fixedDelay: 每次执行间隔时间（以上次执行结束时间为基准），initialDelay: 任务首次执行前的初始延迟
	public void refresh7dCache() {
		refresh7dCache(false);
	}

	// 刷新1小时热点缓存
	public void refresh1hCache(boolean force) {
		// 窗口时间=4.5分钟
		if (!hotNewsActionTracker.shouldRefresh(force, 270_000)) {
			log.info("4.5分钟内无新事件，跳过1h缓存刷新");
			return;
		}

		// 获取当前小时Key
		String currentHourKey = RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getCurrentHour();
		// 获取最近1小时Key（当前小时不算）
		List<String> hourKeys = getRecentHourKeys(1);
		String mergedKey = RedisConstant.HOT_NEWS_HOUR_MERGED_PREFIX + HotNewsUtil.getCurrentHour();

		// 合并前一个小时的数据和当前小时数据到临时Key
		redisUtil.del(mergedKey); // 合并前先清空临时Key
		redisUtil.zUnionAndStore(currentHourKey, hourKeys, mergedKey);

		// 添加随机新闻到缓存
		addRandomNewsToCache(mergedKey);

		cacheTop10(mergedKey, CacheType.HOUR);
		redisUtil.expire(mergedKey, 1, TimeUnit.HOURS); // 保留1小时，设置过期时间防止内存泄漏

		log.info("1小时热点新闻缓存刷新完成");
	}

	// 刷新24小时热点缓存
	public void refresh24hCache(boolean force) {
		// 窗口时间=12.5分钟
		if (!hotNewsActionTracker.shouldRefresh(force, 750_000)) {
			log.info("12.5分钟内无新事件，跳过24h缓存刷新");
			return;
		}

		// 获取当前小时Key
		String currentHourKey = RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getCurrentHour();
		// 获取最近24小时Key（当前小时不算）
		List<String> hourKeys = getRecentHourKeys(24);
		String mergedKey = RedisConstant.HOT_NEWS_24HOUR_MERGED_PREFIX + HotNewsUtil.getCurrentDay();

		// 合并前一天的数据和当前小时数据到临时Key
		redisUtil.del(mergedKey); // 合并前先清空临时Key
		redisUtil.zUnionAndStore(currentHourKey, hourKeys, mergedKey);
		cacheTop10(mergedKey, CacheType.DAY);
		redisUtil.expire(mergedKey, 1, TimeUnit.DAYS); // 保留1天，设置过期时间防止内存泄漏

		log.info("24小时热点新闻缓存刷新完成");
	}

	// 刷新7天热点缓存
	public void refresh7dCache(boolean force) {
		// 窗口时间=35分钟
		if (!hotNewsActionTracker.shouldRefresh(force, 2100_000)) {
			log.info("35分钟内无新事件，跳过周缓存刷新");
			return;
		}

		// 获取当前24小时Key（当前暂存的，即HOT_NEWS_24HOUR_MERGED）
		String current24hKey = RedisConstant.HOT_NEWS_24HOUR_MERGED_PREFIX + HotNewsUtil.getCurrentDay();
		// 获取最近7天Key（今天不算）
		List<String> dayKeys = getRecentDayKeys(7);
		String mergedKey = RedisConstant.HOT_NEWS_WEEK;

		// 合并前7天的数据和当前24小时数据到最近7天热点Key
		redisUtil.del(mergedKey); // 合并前先清空临时Key
		redisUtil.zUnionAndStore(current24hKey, dayKeys, mergedKey);
		cacheTop10(mergedKey, CacheType.WEEK);
		redisUtil.expire(mergedKey, 8, TimeUnit.DAYS); // 保留8天，设置过期时间防止内存泄漏

		log.info("7天热点新闻缓存刷新完成");
	}

	// 如果合并后的1小时热点新闻数为n个（小于10），则随机将10-n个新闻加入到缓存中（增加其浏览量）
	// 防止1小时新闻热点数据为空
	private void addRandomNewsToCache(String mergedKey) {
		long size = redisUtil.zSize(mergedKey);
		if (size >= 10L) {
			log.info("1小时热点新闻缓存已满，跳过随机新闻加入");
			return;
		}
		NewsService.MinAndMaxId minAndMaxId = getMinAndMaxId();
		int needSize = 10 - (int) size;
		Set<Long> candidateIds = new HashSet<>(15); // 最大needSize为10，设置初始容量为15 防止扩容
		while (candidateIds.size() <= needSize) {
			Long newsId = RANDOM.nextLong(minAndMaxId.getMinId(), minAndMaxId.getMaxId() + 1);
			candidateIds.add(newsId);
		}
		candidateIds.forEach(newsId -> {
			hotNewsService.asyncUpdateMetricsAndHotness(new UserActionEvent(newsId, 0L, UserActionEvent.ActionType.VIEW, System.currentTimeMillis()));
		});
		log.info("随机加入{}个新闻到1小时热点新闻缓存", needSize);
	}

	// 加载缓存数据到Java内存
	public void loadCacheTop10() {
		String[] keys = {
				RedisConstant.HOT_NEWS_HOUR_MERGED_PREFIX + HotNewsUtil.getCurrentHour(),
				RedisConstant.HOT_NEWS_24HOUR_MERGED_PREFIX + HotNewsUtil.getCurrentDay(),
				RedisConstant.HOT_NEWS_WEEK
		};
		CacheType[] types = CacheType.values();

		// 收集所有异步任务的 CompletableFuture
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < types.length; i++) {
			CompletableFuture<Void> future = cacheTop10(keys[i], types[i]);
			futures.add(future);
		}

		// 等待所有任务完成
		try {
			log.info("开始加载热点新闻缓存");
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
			log.info("热点新闻缓存加载完成");
			log.info("1小时热点新闻缓存size：{}", getHourCacheTop10().size());
			log.info("24小时热点新闻缓存size：{}", get24HourCacheTop10().size());
			log.info("7天热点新闻缓存size：{}", getWeekCacheTop10().size());
		} catch (InterruptedException | ExecutionException e) {
			// 处理异常（如记录日志或重新抛出）
			Thread.currentThread().interrupt(); // 恢复中断状态
			log.error("热点新闻缓存加载失败", e);
		}
	}


	// 异步执行
	private CompletableFuture<Void> cacheTop10(String sourceKey, CacheType cacheType) {
		return CompletableFuture.runAsync(() -> {
			// 从redis中获取前10个新闻
			Set<ZSetOperations.TypedTuple<Object>> tuples = redisUtil.zReverseRangeWithScores(sourceKey, 0, 9);
			List<HotNews> top10 = tuples.stream()
					.map(t -> new HotNews(Long.parseLong(t.getValue().toString()), t.getScore()))
					.toList();
			int rank = 1;
			for (HotNews hotNews : top10) {
				hotNews.setRank(rank); // 设置排名
				News news = hotNewsService.getNews(hotNews.getNewsId()); // 从redis中获取新闻详情
				if (news != null) {
					hotNews.setNews(news);
				}
				rank++;
			}
			// 根据缓存类型更新对应缓存
			switch (cacheType) {
				case HOUR:
					setHourCacheTop10(top10);
					break;
				case DAY:
					set24HourCacheTop10(top10);
					break;
				case WEEK:
					setWeekCacheTop10(top10);
					break;
			}
		}, globalTaskExecutor); // 使用自定义线程池执行异步任务
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

	/**
	 * 异步获取推荐新闻
	 *
	 * @param size   推荐数量
	 * @param userId 必须显式传递用户ID（禁止在异步方法内使用ThreadLocal）
	 * @return 异步结果包装
	 */
	@Async("globalTaskExecutor")
	public CompletableFuture<List<NewsVo>> getRecommendsAsync(Integer size, long userId, long categoryId) {
		try {
			String zSetKey = RedisConstant.HOT_NEWS_WEEK; // 从七天热点新闻中推荐新闻
			String setKey = RedisConstant.NEWS_RECOMMEND_PREFIX + userId; // 存储用户已被推荐的新闻ID

			// 获取到随机推荐的新闻ID（内部会自动维护用户的已推荐新闻ID集合）
			List<Long> newsIds = redisZSetUtil.getRandomZSetLongs(zSetKey, setKey, categoryId, size);

			List<News> newsList;
			if (!CollectionUtils.isEmpty(newsIds)) {
				newsList = buildFromCache(newsIds);
			} else {
				newsList = buildFromDB(size, userId, categoryId);
				// 更新新闻ID集合
				newsIds = newsList.stream().map(News::getId).toList();
			}
			// 添加用户新闻操作
			List<NewsVo> result = addUserNewsActions(newsIds, newsList, userId);
			// 添加新闻分类标签
			addCategories(newsIds, result);
			return CompletableFuture.completedFuture(result);
		} catch (Exception e) {
			log.error("Async recommendation failed for user {}", userId, e);
			return CompletableFuture.failedFuture(e);
		}
	}

	private List<News> buildFromCache(List<Long> newsIds) {
		return newsIds.stream()
				.map(hotNewsService::getNews)
				.filter(Objects::nonNull)
				.toList();
	}

	private List<News> buildFromDB(int size, long userId, long categoryId) {
		// 从数据库中获取随机新闻（内部会自动维护用户的已推荐新闻ID集合）
		List<News> newsList = newsService.getRecommends(size, userId, categoryId);

		// 异步缓存并处理异常
		CompletableFuture.runAsync(() -> {
			try {
				hotNewsService.batchCacheNewsToRedis(newsList);
			} catch (Exception e) {
				log.error("Failed to cache news for user {}", userId, e);
			}
		}, globalTaskExecutor);

		return newsList;
	}

	public List<NewsVo> addUserNewsActions(List<Long> newsIds, List<News> newsList, long userId) {
		// 获取用户对新闻的操作（比如格式为新闻ID：true）
		Map<String, Map<Object, Boolean>> actionMap = hotNewsService.getUserNewsAction(newsIds.toArray(), userId);
		return newsList.stream()
				.map(news -> new NewsVo(news, NewsVo.UserNewsAction.of(actionMap, news.getId())))
				.toList();
	}

	public void addCategories(List<Long> newsIds, List<NewsVo> newsVoList) {
		Map<Long, List<String>> newsCategoryRelationMap = newsCategoryRelationService.getNewsCategoryRelationMap(newsIds);
		for (NewsVo newsVo : newsVoList) {
			List<String> categories = newsCategoryRelationMap.get(newsVo.getNews().getId());
			newsVo.setCategories(categories);
		}
	}

	// 缓存类型：1小时、24小时、7天
	public enum CacheType {
		HOUR,
		DAY,
		WEEK
	}
}