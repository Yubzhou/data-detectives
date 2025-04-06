package com.yubzhou.config;

import com.yubzhou.consumer.HotNewsCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheInitializer implements ApplicationRunner {

	private final HotNewsCacheService hotNewsCacheService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 加载热点新闻缓存到Java内存中
		hotNewsCacheService.loadCacheTop10();
	}
}