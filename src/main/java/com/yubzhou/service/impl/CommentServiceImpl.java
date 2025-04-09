package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.mapper.CommentMapper;
import com.yubzhou.model.dto.CreateCommentDto;
import com.yubzhou.model.dto.QueryCommentDto;
import com.yubzhou.model.po.Comment;
import com.yubzhou.service.CommentService;
import com.yubzhou.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

	private final RedisUtil redisUtil;
	private final ThreadPoolTaskExecutor globalTaskExecutor;

	@Autowired
	public CommentServiceImpl(RedisUtil redisUtil,
							  @Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor) {
		this.redisUtil = redisUtil;
		this.globalTaskExecutor = globalTaskExecutor;
	}

	@Override
	public IPage<Comment> listUserComments(Long userId, QueryCommentDto queryDto) {
		return listCommentsHandler(userId, null, queryDto);
	}

	@Override
	public IPage<Comment> listNewsComments(Long newsId, Long userId, QueryCommentDto queryDto) {
		IPage<Comment> comments = listCommentsHandler(null, newsId, queryDto);
		Object[] commentIds = comments.getRecords().stream().map(Comment::getId).toArray();
		// 批量查询用户点赞记录
		if (commentIds.length > 0) {
			Map<Object, Boolean> isLikes = redisUtil.sHasKeys(RedisConstant.USER_COMMENT_LIKE_PREFIX + userId, commentIds);
			log.debug("用户点赞记录：{}", isLikes);
			comments.getRecords().forEach(comment -> comment.setIsLiked(isLikes.getOrDefault(comment.getId(), false)));
		}
		return comments;
	}

	@Override
	public boolean createComment(Long userId, Long newsId, CreateCommentDto createDto) {
		Comment comment = createDto.toEntity(userId, newsId);
		return this.save(comment);
	}

	@Override
	public boolean deleteComment(Long commentId, Long userId) {
		return this.lambdaUpdate()
				.eq(Comment::getId, commentId)
				.eq(Comment::getUserId, userId) // 只能删除自己的评论
				.remove();
	}

	/**
	 * 处理点赞
	 *
	 * @param id     评论ID
	 * @param userId 点赞的用户ID
	 * @return 点赞成功返回true，取消点赞成功返回false
	 */
	@Override
	public boolean likeComment(Long id, long userId) {
		String setKey = RedisConstant.USER_COMMENT_LIKE_PREFIX + userId;
		// 判断是否已经点赞
		boolean isLiked = redisUtil.sHasKey(setKey, id);
		if (isLiked) {
			redisUtil.sRemove(setKey, id);
		} else {
			redisUtil.sSetAndTime(setKey, RedisConstant.USER_COMMENT_LIKE_EXPIRE_TIME, id);
		}

		// 异步更新数据库点赞数
		updateLikesAsync(id, !isLiked);

		// 返回点赞结果（true表示点赞成功，false表示取消点赞成功）
		return !isLiked;
	}

	// 异步处理点赞数量
	public void updateLikesAsync(Long id, boolean isIncrement) {
		// 异步点赞评论
		CompletableFuture.runAsync(() -> {
			this.lambdaUpdate()
					.eq(Comment::getId, id)
					.setIncrBy(Comment::getLikes, isIncrement ? 1 : -1)
					.update();
		}, globalTaskExecutor); // 使用自定义线程池执行异步任务
	}

	private IPage<Comment> listCommentsHandler(Long userId, Long newsId, QueryCommentDto queryDto) {
		// 设置默认分页参数
		if (queryDto.getPageNum() == null || queryDto.getPageNum() <= 0) {
			queryDto.setPageNum(1);
		}
		if (queryDto.getPageSize() == null || queryDto.getPageSize() <= 0) {
			queryDto.setPageSize(10);
		}

		// 构建分页对象
		IPage<Comment> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());

		// 构建查询条件
		LambdaQueryWrapper<Comment> wrapper = Wrappers.lambdaQuery();

		// 动态设置查询条件
		if (userId != null) {
			wrapper.eq(Comment::getUserId, userId);
		} else if (newsId != null) {
			wrapper.eq(Comment::getNewsId, newsId);
		} else {
			throw new IllegalArgumentException("Either userId or newsId must be provided");
		}

		// 排序逻辑优化
		QueryCommentDto.SortBy sortBy = queryDto.getSortBy();
		if (sortBy == QueryCommentDto.SortBy.TIME) {
			wrapper.orderByDesc(Comment::getCreatedAt);
		} else if (sortBy == QueryCommentDto.SortBy.LIKES) {
			wrapper.orderByDesc(Comment::getLikes);
		}

		// 异常处理
		try {
			return page(page, wrapper);
		} catch (Exception e) {
			log.error("查询评论列表失败", e);
			return new Page<>();
		}
	}
}