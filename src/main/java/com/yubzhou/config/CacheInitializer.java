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
public class CacheInitializer implements ApplicationRunner {

	private final HotNewsCacheService hotNewsCacheService;
	private final NewsCategoryService newsCategoryService;
	private final NewsCategoryRelationService newsCategoryRelationService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 加载热点新闻缓存到Java内存中
		hotNewsCacheService.loadCacheTop10();
		// 从数据库中加载新闻分类缓存到redis和Java内存中
		newsCategoryService.loadCacheNewsCategories();
		// 从数据库中加载新闻分类关系缓存到redis中
		newsCategoryRelationService.cacheAllNewsCategoryRelationToRedis();
	}
}