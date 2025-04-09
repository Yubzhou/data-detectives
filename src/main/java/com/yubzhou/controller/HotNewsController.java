package com.yubzhou.controller;

import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.consumer.HotNewsCacheService;
import com.yubzhou.model.pojo.HotNews;
import com.yubzhou.model.vo.NewsVo;
import com.yubzhou.service.NewsCategoryService;
import com.yubzhou.util.WebContextUtil;
import jakarta.validation.constraints.Max;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

	// 随机推荐新闻列表
	@GetMapping("/recommend")
	public CompletableFuture<Result<List<NewsVo>>> recommend(@RequestParam(name = "size", defaultValue = "10")
															 @Max(value = 25, message = "size不能大于25")
															 Integer size,
															 @RequestParam(name = "category", required = false) // 允许为空，为空时默认为所有新闻分类
															 String category) {
		// 当未传递新闻分类名字时，代表没有选择分类，则默认可以推荐各种新闻（即categoryId为0）
		long categoryId = category == null ? 0 : newsCategoryService.getNewsCategoryId(category);
		// 必须在主线程获取用户ID（异步方法无法访问ThreadLocal）
		long userId = WebContextUtil.getCurrentUserId();
		return hotNewsCacheService.getRecommendsAsync(size, userId, categoryId)
				.thenApply(result -> {
					log.info("Successfully generated {} recommendations", result.size());
					return Result.success(result);
				})
				.exceptionally(ex -> {
					log.error("Recommendation failed for user {}", userId, ex);
					return Result.fail(ReturnCode.RC500.getCode(), "系统出错：推荐新闻列表功能不可用", null);
				});
	}
}