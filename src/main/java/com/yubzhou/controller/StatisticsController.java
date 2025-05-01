package com.yubzhou.controller;

import com.yubzhou.common.Result;
import com.yubzhou.model.vo.AchievementVo;
import com.yubzhou.service.CommentService;
import com.yubzhou.service.DetectionRecordService;
import com.yubzhou.util.WebContextUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

	private final DetectionRecordService detectionRecordService;
	private final CommentService commentService;
	private final ThreadPoolTaskExecutor globalTaskExecutor;

	public StatisticsController(DetectionRecordService detectionRecordService,
								CommentService commentService,
								@Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor) {
		this.detectionRecordService = detectionRecordService;
		this.commentService = commentService;
		this.globalTaskExecutor = globalTaskExecutor;
	}

	// 用户我的成就接口（同步接口）：用户新闻检测总次数、用户最长连续检测天数、用户检测记录中真新闻占比、是否为年度达人、用户评论总数
	// @GetMapping("/achievement")
	// public Result<?> getAchievement() {
	// 	// 获取当前用户ID
	// 	long userId = WebContextUtil.getCurrentUserId();
	// 	// 获取用户评论总数
	// 	Long totalComments = commentService.getCommentCountByUserId(userId);
	// 	// 获取用户的最长连续检测天数
	// 	Long maxContinuousDays = detectionRecordService.getMaxContinuousDaysFromCache(userId);
	// 	// 获取用户的总检测次数、检测结果为真新闻的数量、今年检测总天数
	// 	AchievementVo achievement = detectionRecordService.getAchievement(userId);
	// 	achievement.setTotalComments(totalComments);
	// 	achievement.setMaxContinuousDays(maxContinuousDays);
	// 	return Result.success("获取我的成就统计数据成功", achievement);
	// }

	// 用户我的成就接口（异步接口）：用户新闻检测总次数、用户最长连续检测天数、用户检测记录中真新闻占比、是否为年度达人、用户评论总数
	@GetMapping("/achievement")
	public CompletableFuture<Result<?>> getAchievement() {
		// 获取当前用户ID
		long userId = WebContextUtil.getCurrentUserId();

		// 定义临时对象包装前两个查询结果
		// 局部内部类：每次方法调用都会生成一个新的局部内部类实例，但不会重复加载类本身
		class TempResult {
			Long totalComments;
			Long maxContinuousDays;
		}

		// 异步任务1：获取评论总数和最长连续天数（同一个线程顺序执行）
		CompletableFuture<TempResult> future1 = CompletableFuture.supplyAsync(() -> {
			TempResult temp = new TempResult();
			temp.totalComments = commentService.getCommentCountByUserId(userId);
			temp.maxContinuousDays = detectionRecordService.getMaxContinuousDaysFromCache(userId);
			return temp;
		}, globalTaskExecutor);

		// 异步任务2：获取成就主体数据（另一个线程执行）
		CompletableFuture<AchievementVo> future2 = CompletableFuture.supplyAsync(() -> detectionRecordService.getAchievement(userId), globalTaskExecutor);

		// 合并两个异步任务结果
		return future1.thenCombineAsync(future2, (tempResult, achievement) -> {
			achievement.setTotalComments(tempResult.totalComments);
			achievement.setMaxContinuousDays(tempResult.maxContinuousDays);
			return Result.success("获取我的成就统计数据成功", achievement);
		}, globalTaskExecutor);
	}
}
