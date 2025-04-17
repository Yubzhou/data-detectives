package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.mapper.NewsCategoryMapper;
import com.yubzhou.model.po.NewsCategory;
import com.yubzhou.service.NewsCategoryService;
import com.yubzhou.util.RedisLockUtil;
import com.yubzhou.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NewsCategoryServiceImpl
		extends ServiceImpl<NewsCategoryMapper, NewsCategory>
		implements NewsCategoryService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisUtil redisUtil;
	private final RedisLockUtil redisLockUtil;

	@Autowired
	public NewsCategoryServiceImpl(RedisTemplate<String, Object> redisTemplate, RedisUtil redisUtil, RedisLockUtil redisLockUtil) {
		this.redisTemplate = redisTemplate;
		this.redisUtil = redisUtil;
		this.redisLockUtil = redisLockUtil;
	}

	// 在系统启动时缓存新闻分类到Redis和Java内存中
	@Override
	public void loadCacheNewsCategories() {
		List<NewsCategory> newsCategories = getAllNewsCategories();
		log.info("从数据库获取到全部新闻分类：{}", newsCategories);
		// 缓存新闻分类到Redis中
		cacheNewsCategoriesToRedis(newsCategories);
		// 缓存新闻分类到Java内存中
		setNewsCategoryCache(newsCategories);
		setNewsCategoryIdCache(newsCategories);
	}

	/**
	 * 查询根据分类ID查询新闻分类
	 * （优先从Java内存中获取，获取不到再到redis或数据库中获取）
	 */
	@Override
	public NewsCategory getNewsCategory(Long categoryId) {
		// 优先从Java内存中获取
		NewsCategory newsCategory = getNewsCategoryFromCache(categoryId);
		if (newsCategory != null) {
			return newsCategory;
		}
		// 如果Java内存获取不到，从Redis或数据库中获取
		return getNewsCategoryFromRedisOrDB(categoryId);
	}

	@Override
	public Long getNewsCategoryId(String categoryName) {
		log.debug("查询新闻分类：{}", categoryName);
		// 优先从Java内存中获取
		Long newsCategoryId = NEWS_CATEGORY_ID_CACHE.get(categoryName);
		if (newsCategoryId == null) {
			throw new BusinessException(ReturnCode.RC400.getCode(), "新闻分类不存在");
		}
		return newsCategoryId;
	}

	private NewsCategory getNewsCategoryFromCache(Long categoryId) {
		return NEWS_CATEGORY_CACHE.get(categoryId);
	}

	private void setNewsCategoryCache(List<NewsCategory> newsCategories) {
		synchronized (NEWS_CATEGORY_CACHE) {
			NEWS_CATEGORY_CACHE.clear();
			for (NewsCategory newsCategory : newsCategories) {
				NEWS_CATEGORY_CACHE.put(newsCategory.getId(), newsCategory);
			}
		}
	}

	private void setNewsCategoryIdCache(List<NewsCategory> newsCategories) {
		synchronized (NEWS_CATEGORY_ID_CACHE) {
			NEWS_CATEGORY_ID_CACHE.clear();
			for (NewsCategory newsCategory : newsCategories) {
				NEWS_CATEGORY_ID_CACHE.put(newsCategory.getName(), newsCategory.getId());
			}
		}
	}

	// 根据新闻分类ID查询新闻分类
	private NewsCategory loadOneFromMySQL(Long categoryId) {
		return this.lambdaQuery()
				.select(NewsCategory::getId, NewsCategory::getName)
				.eq(NewsCategory::getId, categoryId)
				.last("LIMIT 1")
				.oneOpt()
				.orElse(null);
	}

	// 获取全部新闻分类
	private List<NewsCategory> getAllNewsCategories() {
		return this.list();
	}

	private Map<String, String> toMap(List<NewsCategory> newsCategories) {
		// 将List<NewsCategory>转换为Map<String, String>
		return newsCategories.stream()
				.collect(Collectors.toMap(
						newsCategory -> String.valueOf(newsCategory.getId()),    // 键：id（Long）,将其转为字符串
						NewsCategory::getName, // 值：name（String）
						(existingValue, newValue) -> existingValue // 处理重复键（保留旧值）
				));
	}

	// 查询根据分类ID查询新闻分类
	private NewsCategory getNewsCategoryFromRedisOrDB(Long categoryId) {
		String categoryKey = RedisConstant.NEWS_CATEGORY_META;
		int maxRetries = 2; // 最大重试次数
		int retryCount = 0; // 当前重试次数

		while (retryCount < maxRetries) {
			// 第一次检查缓存
			Object name = redisUtil.hget(categoryKey, String.valueOf(categoryId));
			if (name != null) {
				return !"NULL".equals(name) ? NewsCategory.of(categoryId, name.toString()) : null;
			}

			String lockKey = RedisConstant.NEWS_CATEGORY_LOCK_PREFIX + categoryId;
			String lockValue = null;
			try {
				// 尝试获取锁（等待50ms，锁持有10秒）
				lockValue = redisLockUtil.tryLock(lockKey, 50, 10_000);
				if (lockValue != null) {
					// 二次检查缓存
					name = redisUtil.hget(categoryKey, String.valueOf(categoryId));
					if (name != null) {
						return !"NULL".equals(name) ? NewsCategory.of(categoryId, name.toString()) : null;
					}

					// 查询数据库
					NewsCategory newsCategory = loadOneFromMySQL(categoryId);
					if (newsCategory != null) {
						// 缓存到Redis中
						redisUtil.hset(categoryKey, String.valueOf(categoryId), newsCategory.getName());
						return newsCategory;
					} else {
						// 缓存空值防止缓存穿透（查询一个数据库中不存在的数据，导致每次请求都会穿透缓存直接访问数据库。）
						redisUtil.hset(categoryKey, String.valueOf(categoryId), "NULL");
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

	// 在系统启动时缓存新闻分类到Redis中
	private void cacheNewsCategoriesToRedis(List<NewsCategory> newsCategories) {
		// 获取全部新闻分类
		Map<String, String> newsCategoryMap = toMap(newsCategories);

		if (CollectionUtils.isEmpty(newsCategoryMap)) {
			log.warn("新闻分类为空");
			return;
		}
		redisTemplate.opsForHash().putAll(RedisConstant.NEWS_CATEGORY_META, newsCategoryMap);
		log.debug("缓存新闻分类成功");
	}
}
