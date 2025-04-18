package com.yubzhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.common.MinAndMaxId;
import com.yubzhou.model.po.News;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NewsService extends IService<News> {
	MinAndMaxId getMinAndMaxId();

	News findNewsById(@NonNull Long newsId);

	List<News> findNewsByIds(List<Long> newsIds);

	boolean updateMetricsWithVersion(News news);

	List<News> getRecommends(int size, long userId, long categoryId);

	CompletableFuture<List<News>> getRecommendsAsync(int size, Long userId, long categoryId);

	List<News> searchNewsWithRelevance(String keyword, int limit);
}
