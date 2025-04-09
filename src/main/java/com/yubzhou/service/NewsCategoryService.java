package com.yubzhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.po.NewsCategory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface NewsCategoryService extends IService<NewsCategory> {

	// 存储新闻分类到Java内存
	Map<Long, NewsCategory> NEWS_CATEGORY_CACHE = new ConcurrentHashMap<>(); // 分类id：分类对象
	Map<String, Long> NEWS_CATEGORY_ID_CACHE = new ConcurrentHashMap<>(); // 分类名：分类id

	default Map<Long, NewsCategory> getNewsCategoryCache() {
		return NEWS_CATEGORY_CACHE;
	}

	default Map<String, Long> getNewsCategoryIdCache() {
		return NEWS_CATEGORY_ID_CACHE;
	}

	void loadCacheNewsCategories();

	NewsCategory getNewsCategory(Long categoryId);

	Long getNewsCategoryId(String categoryName);
}
