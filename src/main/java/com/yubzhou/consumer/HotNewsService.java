package com.yubzhou.consumer;

import com.yubzhou.common.HotNewsActionTracker;
import com.yubzhou.common.KafkaConstant;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.common.UserActionEvent;
import com.yubzhou.common.UserActionEvent.ActionType;
import com.yubzhou.model.po.News;
import com.yubzhou.service.NewsCategoryRelationService;
import com.yubzhou.service.NewsService;
import com.yubzhou.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HotNewsService {
	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisUtil redisUtil;
	private final RedisLockUtil redisLockUtil;
	private final NewsService newsService;
	private final NewsCategoryRelationService newsCategoryRelationService;
	private final HotNewsActionTracker hotNewsActionTracker;
	private final ThreadPoolTaskExecutor globalTaskExecutor;

	public HotNewsService(RedisTemplate<String, Object> redisTemplate,
						  RedisUtil redisUtil,
						  RedisLockUtil redisLockUtil,
						  NewsService newsService,
						  NewsCategoryRelationService newsCategoryRelationService,
						  HotNewsActionTracker hotNewsActionTracker,
						  @Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor) {
		this.redisTemplate = redisTemplate;
		this.redisUtil = redisUtil;
		this.redisLockUtil = redisLockUtil;
		this.newsService = newsService;
		this.newsCategoryRelationService = newsCategoryRelationService;
		this.hotNewsActionTracker = hotNewsActionTracker;
		this.globalTaskExecutor = globalTaskExecutor;
	}

	public static final Random random = new Random();

	// 监听用户行为事件
	@KafkaListener(topics = KafkaConstant.USER_ACTION_TOPIC, groupId = KafkaConstant.USER_ACTION_GROUP_ID)
	public void processEvent(UserActionEvent event) {
		log.info("Received user action event: {}", event);
		// 更新新闻指标和热度
		asyncUpdateMetricsAndHotness(event);
	}

	// 获取新闻详情，返回News
	public News getNews(Long newsId) {
		String newsKey = RedisConstant.NEWS_DETAIL_PREFIX + newsId;
		int maxRetries = 2; // 最大重试次数
		int retryCount = 0; // 当前重试次数

		while (retryCount < maxRetries) {
			// 第一次检查缓存
			Map<Object, Object> cachedNews = redisUtil.hmget(newsKey);
			if (!CollectionUtils.isEmpty(cachedNews)) {
				return !"NULL".equals(cachedNews.get("title")) ? News.fromRedisMap(cachedNews) : null;
			}

			String lockKey = RedisConstant.NEWS_LOCK_PREFIX + newsId;
			String lockValue = null;
			try {
				// 尝试获取锁（等待50ms，锁持有10秒）
				lockValue = redisLockUtil.tryLock(lockKey, 50, 10_000);
				if (lockValue != null) {
					// 二次检查缓存
					cachedNews = redisUtil.hmget(newsKey);
					if (!CollectionUtils.isEmpty(cachedNews)) {
						return !"NULL".equals(cachedNews.get("title")) ? News.fromRedisMap(cachedNews) : null;
					}

					// 查询数据库
					News news = loadOneFromMySQL(newsId);
					if (news != null) {
						// 异步缓存到Redis中
						globalTaskExecutor.execute(() -> {
							// 缓存新闻详情
							cacheNewsToRedis(news);
							// 缓存新闻-分类关系
							newsCategoryRelationService.cacheNewsCategoryRelationToRedis(newsId);
						});
						return news;
					} else {
						// 缓存空值防止缓存穿透（查询一个数据库中不存在的数据，导致每次请求都会穿透缓存直接访问数据库。）
						redisUtil.hmset(newsKey, Collections.singletonMap("title", "NULL"));
						redisUtil.expire(newsKey, 5, TimeUnit.MINUTES);
						return null;
					}
				} else {
					// 降级策略：等待后重试
					TimeUnit.MILLISECONDS.sleep(50);
					retryCount++;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("锁获取中断", e);
			} finally {
				if (lockValue != null) {
					redisLockUtil.unlock(lockKey, lockValue);
				}
			}
		}
		// 达到最大重试次数后降级
		return null;
	}

	// 获取用户对某些新闻列表的操作（比如格式为新闻ID：true）
	public Map<String, Map<Object, Boolean>> getUserNewsAction(List<News> newsList, long userId) {
		if (CollectionUtils.isEmpty(newsList)) return Collections.emptyMap();

		// 获取到新闻ID列表
		Object[] newsIds = newsList.stream().map(News::getId).toArray();
		return getUserNewsAction(newsIds, userId);
	}

	// 获取用户对某些新闻列表的操作（比如格式为新闻ID：true）
	public Map<String, Map<Object, Boolean>> getUserNewsAction(Object[] newsIds, long userId) {
		if (newsIds == null || newsIds.length == 0) return Collections.emptyMap();

		String userKeyPrefix = RedisConstant.USER_NEWS_ACTION_PREFIX + userId + ":";

		Map<Object, Boolean> supports = redisUtil.sHasKeys(userKeyPrefix + ActionType.SUPPORT.getField(), newsIds);
		Map<Object, Boolean> opposes = redisUtil.sHasKeys(userKeyPrefix + ActionType.OPPOSE.getField(), newsIds);
		Map<Object, Boolean> favorites = redisUtil.sHasKeys(userKeyPrefix + ActionType.FAVORITE.getField(), newsIds);

		Map<String, Map<Object, Boolean>> userNewsAction = new HashMap<>();
		userNewsAction.put(ActionType.SUPPORT.getField(), supports);
		userNewsAction.put(ActionType.OPPOSE.getField(), opposes);
		userNewsAction.put(ActionType.FAVORITE.getField(), favorites);

		log.info("User {} action: {}", userId, userNewsAction);

		return userNewsAction;
	}

	// 将从MySQL中获取的新闻缓存到Redis中
	public void cacheNewsToRedis(News news) {
		if (news == null) return;
		String newsKey = RedisConstant.NEWS_DETAIL_PREFIX + news.getId();
		redisUtil.hmset(newsKey, news.toRedisMap());
		// 防止缓存雪崩（大量缓存Key同时过期，或Redis服务宕机，导致所有请求直接访问数据库）
		// 随机设置缓存过期时间（2-5小时）
		redisUtil.expire(newsKey, random.nextInt(120, 300), TimeUnit.MINUTES);
	}

	// 批量将从MySQL中获取的新闻缓存到Redis中
	public void batchCacheNewsToRedis(List<News> newsList) {
		if (CollectionUtils.isEmpty(newsList)) return;
		redisTemplate.executePipelined(new SessionCallback<>() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				HashOperations<String, String, Object> hashOps = operations.opsForHash();
				for (News news : newsList) {
					if (news == null || news.getId() == null) continue;
					hashOps.putAll(RedisConstant.NEWS_DETAIL_PREFIX + news.getId(), news.toRedisMap());
				}
				return null; // 返回null表示不需要返回结果
			}
		});
	}

	private News loadOneFromMySQL(Long newsId) {
		return newsService.findNewsById(newsId);
	}

	// 异步更新新闻指标并刷新1小时热度
	public void asyncUpdateMetricsAndHotness(UserActionEvent event) {
		// 标记最近一次用户行为事件
		hotNewsActionTracker.markAction();

		globalTaskExecutor.execute(() -> {
			// 强制触发缓存加载（防止redis中缓存失效）
			News news = getNews(event.getNewsId());
			if (news == null) {
				log.error("redis和数据库中新闻数据都不存在，忽略该事件: {}", event);
				return;
			}

			// 获取新闻指标Key
			String newsKey = RedisConstant.NEWS_DETAIL_PREFIX + event.getNewsId();
			// 确保互斥操作（即假如同一用户对同一新闻之前进行了反对操作，现在要进行支持操作，则需要先取消之前的反对操作）
			ensureMutualExclusiveAction(newsKey, event);
			// 更新Hash中的新闻指标（浏览量、评论数、收藏数等）
			updateMetrics(newsKey, event);
			// 更新用户新闻行为记录
			updateUserNewsAction(event);
			// 更新热度
			updateHotness(newsKey, event);
			// 更新数据库
			updateMetricsToMySQL(newsKey, event);
		});
	}

	// 异步执行数据库更新
	private void updateMetricsToMySQL(String newsKey, UserActionEvent event) {
		try {
			// 从redis中获取指定新闻的指标
			Map<Object, Object> newsMap = redisUtil.hmget(newsKey);
			// 转换为News对象
			News news = News.fromRedisMapForMetrics(newsMap);

			// 使用CAS乐观锁更新
			boolean success = newsService.updateMetricsWithVersion(news);
			// 如果数据库更新成功，则更新redis中的版本号
			if (success) {
				int currentVersion = news.getVersion();
				redisUtil.hset(newsKey, "version", currentVersion + 1);
				log.info("异步更新数据库新闻指标成功（newsId={}），最新版本号: {}", news.getId(), currentVersion + 1);
			} else { // 如果失败，说明版本号有冲突，需要重新同步
				News dbNews = newsService.findNewsById(event.getNewsId());
				if (dbNews != null) {
					// 将数据库最新数据写回 Redis，并更新版本号
					redisUtil.hmset(newsKey, dbNews.toRedisMap());
					redisUtil.expire(newsKey, random.nextInt(120, 300), TimeUnit.MINUTES);
					log.warn("异步更新数据库新闻指标失败（newsId={}），已将数据库中最新数据同步到redis，最新版本号: {}", dbNews.getId(), dbNews.getVersion());
					return;
				}
				// 如果dbNews为null，则说明数据库中不存在该新闻，但是redis中存在，则直接删除redis中的数据
				redisUtil.del(newsKey);
				log.warn("异步更新数据库新闻指标失败（newsId={}），数据库中不存在该新闻，已删除redis中的数据", event.getNewsId());
			}
		} catch (Exception e) {
			log.error("异步更新数据库新闻指标失败（newsId={}）", event.getNewsId(), e);
		}
	}

	// 如果是支持或反对操作，则需要先检查是否存在互斥操作（即假如同一用户对同一新闻之前进行了反对操作，现在要进行支持操作，则需要先取消之前的反对操作）
	private void ensureMutualExclusiveAction(String newsKey, UserActionEvent event) {
		final ActionType action = event.getAction();
		long remove;
		// 如果是支持或反对操作，则需要先检查是否存在互斥操作（即假如同一用户对同一新闻之前进行了反对操作，现在要进行支持操作，则需要先取消之前的反对操作）
		switch (action) {
			case SUPPORT:
				// 移除相反操作
				remove = redisUtil.sRemove(RedisConstant.USER_NEWS_ACTION_PREFIX + event.getUserId() + ":" + ActionType.OPPOSE.getField(), event.getNewsId());
				if (remove > 0) {
					// 如果存在相反操作，则将其数量减少
					redisUtil.hincr(newsKey, ActionType.OPPOSE.getField(), -1);
				}
				break;
			case OPPOSE:
				remove = redisUtil.sRemove(RedisConstant.USER_NEWS_ACTION_PREFIX + event.getUserId() + ":" + ActionType.SUPPORT.getField(), event.getNewsId());
				if (remove > 0) {
					redisUtil.hincr(newsKey, ActionType.SUPPORT.getField(), -1);
				}
				break;
		}
	}

	// 更新新闻指标（浏览量、评论数、收藏数等）
	private void updateMetrics(String newsKey, UserActionEvent event) {
		int delta = event.getAction().isCancelAction() ? -1 : 1;
		redisUtil.hincr(newsKey, event.getAction().getField(), delta);
	}

	private void updateHotness(String newsKey, UserActionEvent event) {
		// 计算当前热度分
		Map<Object, Object> newsMap = redisUtil.hmget(newsKey);
		double score = calculateHotness(newsMap);

		// 更新1小时窗口Sorted Set（过期时间25小时）
		String hourKey = getCurrentHourKey();
		redisUtil.zAdd(hourKey, event.getNewsId(), score);
		redisUtil.expire(hourKey, 25, TimeUnit.HOURS);
	}

	/**
	 * 更新用户新闻行为记录（即用户对某一新闻是否进行了支持、反对、收藏等操作，以及取消支持、取消反对、取消收藏等操作）
	 * 记录格式：user_news_action:userId:action newsId，如user_news_action:1:supports 1表示用户1对新闻1进行了支持
	 * 存储类型为 Redis Set，以便快速查询某个用户是否对某一新闻进行过某种操作
	 * 用来判断某个用户是否对某一新闻进行过某种操作（如是否对某一新闻进行了支持、反对、收藏等操作）
	 */
	private void updateUserNewsAction(UserActionEvent event) {
		final ActionType action = event.getAction();
		boolean isCancel = action.isCancelAction();

		// 1. 确定实际需要处理的操作类型
		ActionType processedAction = isCancel ?
				ActionType.from(action.name().substring(2)) : // 转换取消操作到原操作
				action;

		// 2. 如果不为支持、反对、收藏等操作，则直接返回（因为其他动作无需记录）
		if (!processedAction.isSupportOrOpposeOrFavorite()) return;

		// 3. 构建Redis Key
		String actionKey = RedisConstant.USER_NEWS_ACTION_PREFIX + event.getUserId() + ":" + processedAction.getField();

		// 4. 根据是否为取消操作，进行对应操作
		if (isCancel) redisUtil.sRemove(actionKey, event.getNewsId());
		else redisUtil.sSet(actionKey, event.getNewsId());
	}

	private String getCurrentHourKey() {
		return RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getCurrentHour();
	}

	// 计算热度分
	private double calculateHotness(Map<Object, Object> newsMap) {
		// 计算基础热度
		double base = HotNewsUtil.calculateBaseHotness(newsMap);
		LocalDateTime createdAt = DateTimeUtil.parseLocalDateTimeNoMillis(newsMap.get("createdAt").toString());
		// 计算衰减热度
		return HotNewsUtil.calculateDecayedHotness(base, createdAt);
	}
}