package com.yubzhou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yubzhou.common.Result;
import com.yubzhou.common.UserActionEvent;
import com.yubzhou.consumer.HotNewsService;
import com.yubzhou.model.dto.CreateCommentDto;
import com.yubzhou.model.dto.QueryCommentDto;
import com.yubzhou.model.po.Comment;
import com.yubzhou.model.vo.CommentVo;
import com.yubzhou.service.CommentService;
import com.yubzhou.util.WebContextUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@Validated
public class CommentController {

	private final CommentService commentService;
	private final HotNewsService hotNewsService;


	public CommentController(CommentService commentService, HotNewsService hotNewsService) {
		this.commentService = commentService;
		this.hotNewsService = hotNewsService;
	}

	// 用户中心-我的评论列表
	@PostMapping("/user")
	public Result<IPage<CommentVo>> listUserComments(@Valid @RequestBody QueryCommentDto queryDto) {
		long userId = WebContextUtil.getCurrentUserId();
		IPage<CommentVo> commentVos = commentService.listUserComments(userId, queryDto);
		return Result.success(commentVos);
	}

	// 新闻详情页-评论列表
	@PostMapping("/news/{newsId:[1-9]\\d*}")
	public Result<IPage<CommentVo>> listNewsComments(@PathVariable("newsId") Long newsId,
													 @Valid @RequestBody QueryCommentDto queryDto) {
		long userId = WebContextUtil.getCurrentUserId();
		IPage<CommentVo> commentVos = commentService.listNewsComments(newsId, userId, queryDto);
		return Result.success(commentVos);
	}

	// 发表评论
	@PostMapping("/post/{newsId:[1-9]\\d*}")
	public Result<?> postComment(@PathVariable("newsId") Long newsId,
								 @Valid @RequestBody CreateCommentDto createDto) {
		long userId = WebContextUtil.getCurrentUserId();
		Comment comment = commentService.createComment(userId, newsId, createDto);
		if (comment == null) return Result.fail("发布评论失败");
		// 如果发布成功，更新新闻指标
		hotNewsService.asyncUpdateMetricsAndHotness(new UserActionEvent(newsId, userId,
				UserActionEvent.ActionType.COMMENT, System.currentTimeMillis()));
		return Result.success("发布评论成功", comment);
	}

	// 删除评论
	@DeleteMapping("/{id:[1-9]\\d*}")
	public Result<Void> deleteComment(@PathVariable("id") Long id,
									  @RequestParam("newsId")
									  @Min(value = 1, message = "新闻ID不能小于1")
									  Long newsId) {
		// 获取用户ID的作用是防止用户删除其他用户的评论，只能删除自己的
		long userId = WebContextUtil.getCurrentUserId();
		boolean success = commentService.deleteComment(id, userId);
		commentService.removeById(id);
		if (!success) return Result.fail("删除评论失败");
		// 如果删除成功，更新新闻指标
		hotNewsService.asyncUpdateMetricsAndHotness(new UserActionEvent(newsId, userId,
				UserActionEvent.ActionType.UNCOMMENT, System.currentTimeMillis()));
		return Result.successWithMessage("删除评论成功");
	}

	// 点赞评论
	@PostMapping("/like")
	public Result<Void> likeComment(@RequestParam("commentId")
									@Min(value = 1, message = "评论ID不能小于1")
									Long commentId) {
		long userId = WebContextUtil.getCurrentUserId();
		// 返回结果为是否为取消点赞成功，还是点赞成功
		boolean isLike = commentService.likeComment(commentId, userId);
		if (!isLike) return Result.fail("取消点赞成功");
		return Result.successWithMessage("点赞成功");
	}
}