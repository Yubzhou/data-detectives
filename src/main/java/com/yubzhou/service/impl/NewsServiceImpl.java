package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.mapper.NewsMapper;
import com.yubzhou.model.po.News;
import com.yubzhou.service.NewsService;
import com.yubzhou.util.RedisUtil;
import com.yubzhou.util.TypeConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


@Service
@Slf4j
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

	private final RedisUtil redisUtil;

	public NewsServiceImpl(RedisUtil redisUtil) {
		this.redisUtil = redisUtil;
	}

	public static final Random random = new Random();

	@Override
	public MinAndMaxId findMinAndMaxId() {
		// SELECT MIN(id) AS minId, MAX(id) AS maxId FROM news
		MinAndMaxId minMax = this.baseMapper.getMinAndMaxId();
		if (minMax == null || minMax.getMinId() == null || minMax.getMaxId() == null) return null;
		return minMax;
	}

	@Override
	public News findNewsById(@NonNull Long newsId) {
		return this.lambdaQuery()
				.eq(News::getId, newsId)
				.last("LIMIT 1")
				.oneOpt()
				.orElse(null);
	}

	@Override
	public List<News> findNewsByIds(List<Long> newsIds) {
		return this.lambdaQuery()
				.select(News::getId, News::getTitle, News::getContent)
				.in(News::getId, newsIds)
				.list();
	}

	@Override
	public boolean updateMetricsWithVersion(News news) {
		if (news.getId() == null) return false;

		// 1. 读取当前版本号
		int currentVersion = news.getVersion();

		// 2. 构建条件
		LambdaUpdateWrapper<News> wrapper = new LambdaUpdateWrapper<News>()
				.eq(News::getId, news.getId())
				.eq(News::getVersion, currentVersion) // 乐观锁，版本号必须一致
				.set(news.getViews() != null, News::getViews, news.getViews())
				.set(news.getSupports() != null, News::getSupports, news.getSupports())
				.set(news.getOpposes() != null, News::getOpposes, news.getOpposes())
				.set(news.getComments() != null, News::getComments, news.getComments())
				.set(news.getFavorites() != null, News::getFavorites, news.getFavorites())
				.set(news.getVersion() != null, News::getVersion, currentVersion + 1); // 乐观锁，版本号加1

		// 3. 判断是否更新成功
		return this.update(news, wrapper);
	}

	// 从数据库中获取推荐新闻（按发布时间获取最新新闻）
	@Override
	public List<News> getRecommends(int size, long userId, long categoryId) {
		try {
			return getRecommendsHandler(size, userId, categoryId);
		} catch (Exception e) {
			log.error("Async recommendation failed for user {}", userId, e);
			return Collections.emptyList();
		}
	}

	/**
	 * 异步获取推荐新闻
	 *
	 * @param size   请求数量
	 * @param userId 必须从上游传递，不能依赖ThreadLocal
	 *               （ThreadLocal 存储的数据是线程私有的，每个线程独立维护自己的数据副本。当异步方法通过 @Async 或线程池切换到新线程时，原线程的 ThreadLocal 数据无法自动传递到新线程。）
	 * @return 异步结果包装
	 */
	@Override
	@Async("globalTaskExecutor")
	public CompletableFuture<List<News>> getRecommendsAsync(int size, Long userId, long categoryId) {
		try {
			List<News> result = getRecommendsHandler(size, userId, categoryId);
			return CompletableFuture.completedFuture(result);
		} catch (Exception e) {
			log.error("Async recommendation failed for user {}", userId, e);
			return CompletableFuture.failedFuture(e);
		}
	}

	private List<News> getRecommendsHandler(int size, long userId, long categoryId) {
		// 1. 获取已推荐ID集合（使用上下文传递的userId）
		String setKey = RedisConstant.NEWS_RECOMMEND_PREFIX + userId;
		Set<Long> excludeIds = TypeConverter.convert(redisUtil.sGet(setKey), Long.class, HashSet::new);

		// 2. 根据是否指定了分类ID，计算出可获取的新闻总数
		// （因为指定了分类ID，则去redis中获取新闻ID；否则去数据库获取新闻ID）
		long total = 0L;
		MinAndMaxId minMax = null;
		if (categoryId == 0L) {
			// 1. 获取新闻ID范围
			minMax = findMinAndMaxId();
			total = minMax.getMaxId() - minMax.getMinId() + 1;
		} else {
			total = redisUtil.sSize(RedisConstant.NEWS_CATEGORY_SET_PREFIX + categoryId);
		}

		// 3. 动态计算策略
		boolean forceMode = shouldUseForceMode(excludeIds.size(), size, total);

		// 4. 执行最终查询
		return getRandomNews(
				setKey,
				categoryId,
				calculateAdjustedSize(size, excludeIds.size(), total, forceMode),
				minMax,
				// 如果为强制模式，则随机推荐新闻（无论是否被推荐过），传递空集合，表示不排除任何新闻ID
				forceMode ? Collections.emptySet() : excludeIds
		);
	}


	// 从数据库中随机获取新闻（内部会自动维护用户已推荐的新闻ID集合）
	private List<News> getRandomNews(String setKey, long categoryId, int size, MinAndMaxId minMax,
									 Set<Long> excludeIds) {
		// 获取候选ID集合
		Set<Long> candidateIds = getCandidateIds(categoryId, size, minMax, excludeIds);

		// 如果指定分类下的新闻已经推荐完了，则直接返回空集合
		if (categoryId != 0L && candidateIds.isEmpty()) return Collections.emptyList();

		List<News> finalNews = null; // 最终返回的新闻

		if (categoryId == 0L) {
			// 转换为List并截取前size个
			List<Long> idsList = new ArrayList<>(candidateIds);
			int actualSize = Math.min(size, idsList.size());
			// 查询数据库（只会返回数据库中已存在的新闻）
			List<News> newsList = this.listByIds(idsList.subList(0, actualSize));

			List<News> moreNews = new ArrayList<>(0);
			// 如果查询到的数量小于等于需要获取数量的一半，则查询剩余0.5*size的新闻
			if (newsList.size() <= size / 2) {
				moreNews = this.listByIds(idsList.subList(actualSize, idsList.size()));
			}
			finalNews = Stream.concat(newsList.stream(), moreNews.stream()).toList();
		} else {
			finalNews = this.listByIds(candidateIds);
		}

		// 维护用户的已推荐新闻的Redis集合
		redisUtil.sSet(
				setKey,
				finalNews.stream().map(News::getId).toArray()
		);
		if (redisUtil.getExpire(setKey) == -1L) {
			// 只有键存在且未设置过期时间，才设置过期时间
			redisUtil.expire(setKey, RedisConstant.NEWS_RECOMMEND_EXPIRE_TIME);
		}

		// 将两次查询的新闻合并
		return finalNews;
	}

	private Set<Long> getCandidateIds(long categoryId, int size, MinAndMaxId minMax, Set<Long> excludeIds) {
		// 生成随机ID列表
		int limit = (int) Math.ceil(size * 1.25);
		Set<Long> candidateIds = new HashSet<>(limit);
		// 如果分类ID为0（代表未指定新闻分类），则从数据库中随机选择
		if (categoryId == 0L) {
			// 如果数量未达到limit，则继续生成随机ID
			while (candidateIds.size() < limit) { // 确保生成的ID数量足够
				long randomId = random.nextLong(minMax.getMinId(), minMax.getMaxId() + 1);
				if (excludeIds.contains(randomId)) continue; // 排除已推荐的新闻
				candidateIds.add(randomId);
			}
		} else {
			// 如果分类ID不为0（代表指定新闻分类），则从指定分类中随机选择
			int attempts = 0; // 尝试次数
			int maxAttempts = 3; // 最大尝试次数
			// 只要获取到需要的size即可
			while (candidateIds.size() < size && attempts < maxAttempts) {
				// 从指定分类下的新闻中随机选择
				Set<Long> randomNewsIds = TypeConverter.convert(
						redisUtil.sGetRandom(RedisConstant.NEWS_CATEGORY_SET_PREFIX + categoryId, limit),
						Long.class,
						HashSet::new
				);
				for (Long newsId : randomNewsIds) {
					if (excludeIds.contains(newsId)) continue; // 排除已推荐的新闻
					candidateIds.add(newsId);
					if (candidateIds.size() >= limit) break;
				}
				attempts++;
			}
		}
		return candidateIds;
	}


	// // 从数据库中随机获取新闻（内部会自动维护用户已推荐的新闻ID集合）
	// private List<News> getRandomNews(String setKey, int size, MinAndMaxId minMax, Set<Long> excludeIds) {
	// 	// 生成随机ID列表
	// 	Set<Long> candidateIds = new HashSet<>();
	// 	int limit = (int) Math.ceil(size * 1.5);
	// 	while (candidateIds.size() < limit) { // 确保生成的ID数量足够
	// 		long randomId = random.nextLong(minMax.getMinId(), minMax.getMaxId() + 1);
	// 		if (excludeIds.contains(randomId)) continue; // 排除已推荐的新闻
	// 		candidateIds.add(randomId);
	// 	}
	//
	// 	// 转换为List并截取前size个
	// 	List<Long> idsList = new ArrayList<>(candidateIds);
	// 	// 查询数据库（只会返回数据库中已存在的新闻）
	// 	List<News> newsList = this.listByIds(idsList.subList(0, size));
	//
	// 	List<News> moreNews = new ArrayList<>(0); // 初始化为空集合
	// 	// 如果查询到的数量小于等于需要获取数量的一半，则查询剩余0.5*size的新闻
	// 	if (newsList.size() <= size / 2) {
	// 		moreNews = this.listByIds(idsList.subList(size, idsList.size()));
	// 	}
	//
	// 	List<News> finalNews = Stream.concat(newsList.stream(), moreNews.stream()).toList();
	//
	// 	// 维护用户的已推荐新闻的Redis集合
	// 	if (redisUtil.getExpire(setKey) == -1L) {
	// 		// 只有键存在且未设置过期时间，才设置过期时间
	// 		redisUtil.sSet(
	// 				setKey,
	// 				RedisConstant.NEWS_RECOMMEND_EXPIRE_TIME,
	// 				finalNews.stream().map(News::getId).toArray()
	// 		);
	// 	}
	//
	// 	// 将两次查询的新闻合并
	// 	return finalNews;
	// }

	// 策略判断逻辑抽取
	private boolean shouldUseForceMode(int excludeSize, int requestSize, long total) {
		// 如果已经推荐过80%的新闻，则直接从数据库中随机获取新闻（无论是否被推荐过）
		// 如果需要获取的数量大于数据库中剩余的数量的一半，则直接从数据库中
		return ((double) (excludeSize + requestSize) / total) > 0.8
				|| (total - excludeSize) <= (requestSize / 2);
	}

	// 数量调整逻辑
	private int calculateAdjustedSize(int requestSize, int excludeSize, long total, boolean forceMode) {
		return forceMode ? Math.min(requestSize, (int) total) : Math.min(requestSize, (int) (total - excludeSize));
	}
}
