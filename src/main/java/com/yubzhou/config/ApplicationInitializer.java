package com.yubzhou.config;

import com.yubzhou.consumer.HotNewsCacheService;
import com.yubzhou.service.NewsCategoryRelationService;
import com.yubzhou.service.NewsCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationInitializer implements ApplicationRunner {

	private final HotNewsCacheService hotNewsCacheService;
	private final NewsCategoryService newsCategoryService;
	private final NewsCategoryRelationService newsCategoryRelationService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 加载缓存
		loadCache();
	}

	private void loadCache() {
		// 加载新闻元数据（新闻最小ID和最大ID）
		hotNewsCacheService.loadCacheNewsMeta();
		// 加载热点新闻缓存到Java内存中
		hotNewsCacheService.loadCacheTop10();
		// 从数据库中加载新闻分类缓存到redis和Java内存中
		newsCategoryService.loadCacheNewsCategories();
		// 从数据库中加载新闻分类关系缓存到redis中
		newsCategoryRelationService.cacheAllNewsCategoryRelationToRedis();
	}
}