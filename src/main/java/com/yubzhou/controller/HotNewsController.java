package com.yubzhou.controller;

import com.yubzhou.common.Result;
import com.yubzhou.consumer.HotNewsCacheService;
import com.yubzhou.model.vo.HotNews;
import com.yubzhou.service.NewsCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@Validated
@Slf4j
public class HotNewsController {
	private final HotNewsCacheService hotNewsCacheService;
	private final NewsCategoryService newsCategoryService;

	@Autowired
	public HotNewsController(HotNewsCacheService hotNewsCacheService, NewsCategoryService newsCategoryService) {
		this.hotNewsCacheService = hotNewsCacheService;
		this.newsCategoryService = newsCategoryService;
	}

	// 查询1小时缓存热点top10
	@GetMapping("/hot/1h/top10")
	public Result<List<HotNews>> get1hHot() {
		List<HotNews> cacheTop10 = hotNewsCacheService.getHourCacheTop10();
		return Result.success(cacheTop10);
	}

	// 查询24小时缓存热点top10
	@GetMapping("/hot/24h/top10")
	public Result<List<HotNews>> get24hHot() {
		List<HotNews> cacheTop10 = hotNewsCacheService.get24HourCacheTop10();
		return Result.success(cacheTop10);
	}

	// 查询7天缓存热点top10
	@GetMapping("/hot/7d/top10")
	public Result<List<HotNews>> get7dHot() {
		List<HotNews> cacheTop10 = hotNewsCacheService.getWeekCacheTop10();
		return Result.success(cacheTop10);
	}
}