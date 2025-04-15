package com.yubzhou.controller;

import com.yubzhou.common.Result;
import com.yubzhou.common.UserActionEvent;
import com.yubzhou.common.UserToken;
import com.yubzhou.consumer.HotNewsCacheService;
import com.yubzhou.consumer.HotNewsService;
import com.yubzhou.service.CommentService;
import com.yubzhou.util.WebContextUtil;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/private")
@Slf4j
public class AdminController {

	private final HotNewsService hotNewsService;
	private final HotNewsCacheService hotNewsCacheService;
	private final CommentService commentService;
	private final ThreadPoolTaskExecutor globalTaskExecutor;
	private final RedisConnectionFactory redisConnectionFactory;

	@Autowired
	public AdminController(HotNewsService hotNewsService, HotNewsCacheService hotNewsCacheService,
						   CommentService commentService,
						   @Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor,
						   RedisConnectionFactory redisConnectionFactory) {
		this.hotNewsService = hotNewsService;
		this.hotNewsCacheService = hotNewsCacheService;
		this.commentService = commentService;
		this.globalTaskExecutor = globalTaskExecutor;
		this.redisConnectionFactory = redisConnectionFactory;
	}

	// 异步刷新缓存
	@GetMapping("/cache")
	public CompletableFuture<Result<?>> refreshHotNewsCache() {
		log.info("强制刷新热点新闻缓存...");
		return CompletableFuture.supplyAsync(() -> {
			try {
				// 刷新1h缓存
				hotNewsCacheService.refresh1hCache(true);
				// 刷新24h缓存
				hotNewsCacheService.refresh24hCache(true);
				// 刷新7d缓存
				hotNewsCacheService.refresh7dCache(true);
				return Result.successWithMessage("热点新闻缓存刷新成功");
			} catch (Exception e) {
				log.error("缓存刷新失败", e);
				return Result.fail("缓存刷新失败: " + e.getMessage());
			}
		}, globalTaskExecutor);
	}

	// 手动触发 Redis RDB 持久化
	@GetMapping("/rdb")
	public Result<?> triggerRdbPersistence() {
		log.info("开始执行Redis RDB持久化...");
		try (RedisConnection connection = redisConnectionFactory.getConnection()) {
			// 执行SAVE命令（同步阻塞）
			connection.serverCommands().save();
			log.info("Redis RDB持久化完成，数据已保存到磁盘");
			return Result.successWithMessage("Redis RDB持久化完成，数据已保存到磁盘");
		} catch (Exception e) {
			log.error("RDB持久化失败", e);
			return Result.fail("RDB持久化失败");
		}
	}

	// 删除指定评论
	@DeleteMapping("/comment/{id:[1-9]\\d*}")
	public Result<?> deleteComment(@PathVariable("id") Long commentId,
								   @RequestParam("newsId")
								   @Min(value = 1, message = "新闻ID不能小于1")
								   Long newsId,
								   @RequestParam("userId")
								   @Min(value = 1, message = "用户ID不能小于1")
								   Long userId) {
		UserToken userToken = WebContextUtil.getUserToken();
		boolean success = commentService.deleteComment(userToken, commentId);
		if (!success) return Result.fail("删除评论失败：权限不足或评论ID不存在");
		// 如果删除成功，异步更新新闻指标
		hotNewsService.asyncUpdateMetricsAndHotness(new UserActionEvent(newsId, userId,
				UserActionEvent.ActionType.UNCOMMENT, System.currentTimeMillis()));
		return Result.successWithMessage("删除评论成功，评论ID：" + commentId);
	}
}