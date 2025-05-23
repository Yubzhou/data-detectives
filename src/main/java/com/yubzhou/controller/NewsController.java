package com.yubzhou.controller;

import com.yubzhou.common.Result;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.common.UserActionEvent.ActionType;
import com.yubzhou.consumer.HotNewsCacheService;
import com.yubzhou.consumer.HotNewsService;
import com.yubzhou.model.po.News;
import com.yubzhou.model.vo.NewsVo;
import com.yubzhou.producer.NewsActionProducer;
import com.yubzhou.service.NewsCategoryRelationService;
import com.yubzhou.service.NewsCategoryService;
import com.yubzhou.service.NewsService;
import com.yubzhou.util.WebContextUtil;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/news")
@Validated // 启用参数验证
@RequiredArgsConstructor
@Slf4j
public class NewsController {

	private final NewsService newsService;
	private final HotNewsService hotNewsService;
	private final HotNewsCacheService hotNewsCacheService;
	private final NewsCategoryService newsCategoryService;
	private final NewsCategoryRelationService newsCategoryRelationService;
	private final NewsActionProducer newsActionProducer;


	// action参数：view、support、oppose、comment、favorite、unsupport、unoppose、uncomment、unfavorite
	@PostMapping("/{newsId:[1-9]\\d*}/action")
	public Result<Void> action(@PathVariable Long newsId, // 限制newsId最小值为1
							   @RequestParam("action") String action) {
		Long userId = WebContextUtil.getCurrentUserId();
		ActionType actionType = ActionType.from(action);
		newsActionProducer.sendActionEvent(newsId, userId, actionType);
		return Result.successWithMessage("操作成功");
	}

	// 随机推荐新闻列表
	@GetMapping("/recommend")
	public CompletableFuture<Result<Map<String, Object>>> recommend(
			@RequestParam(name = "size", defaultValue = "10")
			@Max(value = 25, message = "size不能大于25")
			Integer size,
			@RequestParam(name = "category", required = false) // 允许为空，为空时默认为所有新闻分类
			String category) {
		// 当未传递新闻分类名字或为空时，代表没有选择分类，则默认可以推荐各种新闻（即categoryId为0）
		long categoryId = StringUtils.hasText(category)
				? newsCategoryService.getNewsCategoryId(category) : 0;
		// 必须在主线程获取用户ID（异步方法无法访问ThreadLocal）
		long userId = WebContextUtil.getCurrentUserId();
		return hotNewsCacheService.getRecommendsAsync(size, userId, categoryId)
				.thenApply(result -> {
					// 获取新闻总数（该类别总共有多少新闻）
					long total = newsCategoryRelationService.getNewsCount(categoryId);
					log.info("Successfully generated {} recommendations", result.size());
					return Result.success(Map.of("total", total, "list", result));
				})
				.exceptionally(ex -> {
					log.error("Recommendation failed for user {}", userId, ex);
					return Result.fail(ReturnCode.RC500.getCode(), "系统出错：推荐新闻列表功能不可用", null);
				});
	}

	// 获取收藏新闻列表
	@GetMapping("/favorite")
	public Result<List<NewsVo>> getFavoriteNews() {
		long userId = WebContextUtil.getCurrentUserId();
		List<News> newsList = hotNewsService.getFavoriteNews(userId);
		if (CollectionUtils.isEmpty(newsList)) {
			return Result.success(Collections.emptyList());
		}
		// 构建NewsVo列表
		List<NewsVo> newsVoList = hotNewsCacheService.buildNewsVoList(null, newsList, userId);
		return Result.success(newsVoList);
	}

	// 新闻搜索（根据新闻标题）
	@GetMapping("/search")
	public Result<List<NewsVo>> search(@RequestParam("keyword") String keyword) {
		long userId = WebContextUtil.getCurrentUserId();
		List<News> newsList = newsService.searchNewsWithRelevance(keyword, 5);
		if (CollectionUtils.isEmpty(newsList)) {
			return Result.success(Collections.emptyList());
		}
		// 构建NewsVo列表
		List<NewsVo> newsVoList = hotNewsCacheService.buildNewsVoList(null, newsList, userId);
		return Result.success(newsVoList);
	}

	// 查看新闻详情
	@GetMapping("/{newsId:[1-9]\\d*}")
	public Result<NewsVo> getNewsDetails(@PathVariable("newsId") Long newsId) {
		long userId = WebContextUtil.getCurrentUserId();
		News news = hotNewsService.getNews(newsId);
		if (news == null) {
			return Result.fail(ReturnCode.RC404.getCode(), "新闻不存在", null);
		}
		// 构建NewsVo
		NewsVo newsVo = hotNewsCacheService.buildNewsVoList(List.of(newsId), List.of(news), userId).get(0);
		return Result.success(newsVo);
	}
}