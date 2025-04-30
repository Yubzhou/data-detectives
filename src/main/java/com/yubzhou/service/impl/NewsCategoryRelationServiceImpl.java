package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.mapper.NewsCategoryRelationMapper;
import com.yubzhou.model.po.NewsCategoryRelation;
import com.yubzhou.service.NewsCategoryRelationService;
import com.yubzhou.service.NewsCategoryService;
import com.yubzhou.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NewsCategoryRelationServiceImpl
		extends ServiceImpl<NewsCategoryRelationMapper, NewsCategoryRelation>
		implements NewsCategoryRelationService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final NewsCategoryService newsCategoryService;
	private final NewsService newsService;

	@Autowired
	public NewsCategoryRelationServiceImpl(RedisTemplate<String, Object> redisTemplate, NewsCategoryService newsCategoryService, NewsService newsService) {
		this.redisTemplate = redisTemplate;
		this.newsCategoryService = newsCategoryService;
		this.newsService = newsService;
	}

	// 获取某个新闻的分类列表
	public List<NewsCategoryRelation> getNewsCategoryList(Long newsId) {
		return this.lambdaQuery()
				.select(NewsCategoryRelation::getNewsId, NewsCategoryRelation::getCategoryId)
				.eq(NewsCategoryRelation::getNewsId, newsId)
				.list();
	}

	// 判断新闻ID是否存储在redis的新闻-分类关系中，如果不存在则缓存
	@Override
	public void cacheNewsCategoryRelationToRedis(Long newsId) {
		if (newsId == null) {
			log.warn("新闻ID为空");
			return;
		}

		// 查询某新闻的全部分类
		List<NewsCategoryRelation> newsCategoryRelationList = this.getNewsCategoryList(newsId);
		if (CollectionUtils.isEmpty(newsCategoryRelationList)) {
			log.warn("新闻ID为{}的新闻分类为空", newsId);
			return;
		}

		// 使用 Pipeline 批量操作
		redisTemplate.executePipelined(new SessionCallback<>() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				SetOperations<String, Object> setOps = redisTemplate.opsForSet();
				for (NewsCategoryRelation newsCategoryRelation : newsCategoryRelationList) {
					String categoryKey = RedisConstant.NEWS_CATEGORY_SET_PREFIX + newsCategoryRelation.getCategoryId();
					setOps.add(categoryKey, newsId);
				}
				return null;
			}
		});
		log.debug("缓存新闻ID为{}的新闻分类成功", newsId);
	}

	@Override
	public void cacheAllNewsCategoryRelationToRedis() {
		this.list().forEach(newsCategoryRelation -> {
			String categoryKey = RedisConstant.NEWS_CATEGORY_SET_PREFIX + newsCategoryRelation.getCategoryId();
			redisTemplate.opsForSet().add(categoryKey, newsCategoryRelation.getNewsId());
		});
		log.info("缓存所有新闻-分类关系成功");
	}

	public Map<Long, List<String>> getNewsCategoryRelationMap(List<Long> newsIds) {
		if (CollectionUtils.isEmpty(newsIds)) {
			return Collections.emptyMap();
		}

		// 预转换 newsIds 为数组，避免重复转换
		Long[] newsIdsArray = newsIds.toArray(new Long[0]);
		Map<Long, List<String>> result = new ConcurrentHashMap<>(); // 线程安全结果集

		// 获取分类缓存（假设 categoryIdCache 是 ConcurrentHashMap）
		Map<String, Long> categoryIdCache = newsCategoryService.getNewsCategoryIdCache();

		// 将 Entry 存入列表（保留当前遍历的瞬时顺序）
		List<Map.Entry<String, Long>> entries = new ArrayList<>(categoryIdCache.entrySet());

		// 执行流水线操作
		List<Object> pipelineResults = redisTemplate.executePipelined(new SessionCallback<>() {
			@Override
			public Object execute(RedisOperations operations) {
				SetOperations setOps = operations.opsForSet();
				for (Map.Entry<String, Long> entry : entries) {
					String key = RedisConstant.NEWS_CATEGORY_SET_PREFIX + entry.getValue();
					// 发送批量查询命令（按 entries 的顺序）
					setOps.isMember(key, newsIdsArray);
				}
				return null;
			}
		});

		// 解析流水线结果（顺序与 entries 列表一致）
		for (int i = 0; i < pipelineResults.size(); i++) {
			List<Boolean> membershipResults = (List<Boolean>) pipelineResults.get(i);
			Map.Entry<String, Long> entry = entries.get(i);
			String categoryName = entry.getKey();

			// 遍历每个 newsId 的检查结果
			for (int j = 0; j < membershipResults.size(); j++) {
				if (membershipResults.get(j)) {
					Long newsId = newsIds.get(j);
					result.computeIfAbsent(newsId, k -> new ArrayList<>()).add(categoryName);
				}
			}
		}

		return result;
	}

	/**
	 * 获取指定类别的新闻总数
	 *
	 * @param categoryId 新闻类别ID（当ID为0表示任意新闻类别）
	 * @return 指定新闻类别的新闻总数
	 */
	@Override
	public long getNewsCount(long categoryId) {
		// 当 categoryId 为 0 时，表示查询所有新闻类别的新闻总数
		if (categoryId == 0) {
			return newsService.count();
		}
		// 否则查询指定类别的新闻总数
		return this.lambdaQuery()
				.eq(NewsCategoryRelation::getCategoryId, categoryId)
				.count();
	}
}
