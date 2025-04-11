package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.common.RedisConstant;
import com.yubzhou.common.UserRole;
import com.yubzhou.common.UserToken;
import com.yubzhou.mapper.CommentMapper;
import com.yubzhou.model.dto.CreateCommentDto;
import com.yubzhou.model.dto.QueryCommentDto;
import com.yubzhou.model.po.Comment;
import com.yubzhou.model.po.UserProfile;
import com.yubzhou.model.vo.CommentVo;
import com.yubzhou.service.CommentService;
import com.yubzhou.service.UserProfileService;
import com.yubzhou.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

	private final UserProfileService userProfileService;
	private final RedisUtil redisUtil;
	private final ThreadPoolTaskExecutor globalTaskExecutor;

	@Autowired
	public CommentServiceImpl(UserProfileService userProfileService, RedisUtil redisUtil,
							  @Qualifier("globalTaskExecutor") ThreadPoolTaskExecutor globalTaskExecutor) {
		this.userProfileService = userProfileService;
		this.redisUtil = redisUtil;
		this.globalTaskExecutor = globalTaskExecutor;
	}

	@Override
	public IPage<CommentVo> listUserComments(Long userId, QueryCommentDto queryDto) {
		return listCommentsHandler(userId, null, queryDto);
	}

	@Override
	public IPage<CommentVo> listNewsComments(Long newsId, Long userId, QueryCommentDto queryDto) {
		IPage<CommentVo> commentVos = listCommentsHandler(null, newsId, queryDto);
		Object[] commentIds = commentVos.getRecords().stream().map(vo -> vo.getCommentDetail().getId()).toArray();
		// 批量查询用户点赞记录
		if (commentIds.length > 0) {
			Map<Object, Boolean> isLikes = redisUtil.sHasKeys(RedisConstant.USER_COMMENT_LIKE_PREFIX + userId, commentIds);
			log.debug("用户点赞记录：{}", isLikes);
			commentVos.getRecords().forEach(vo -> vo.getCommentDetail().setIsLiked(isLikes.getOrDefault(vo.getCommentDetail().getId(), false)));
		}
		return commentVos;
	}

	@Override
	public Comment createComment(Long userId, Long newsId, CreateCommentDto createDto) {
		Comment comment = createDto.toEntity(userId, newsId);
		if (this.save(comment)) {
			return comment;
		}
		return null;
	}

	@Override
	public boolean deleteComment(Long commentId, Long userId) {
		LambdaQueryWrapper<Comment> wrapper = Wrappers.lambdaQuery();
		wrapper.eq(Comment::getId, commentId)
				.eq(Comment::getUserId, userId); // 只能删除自己的评论
		// 只有影响行数大于0时才删除成功
		return this.remove(wrapper);
	}

	// 管理员可以强制评论
	@Override
	public boolean deleteComment(UserToken userToken, Long commentId) {
		if (userToken.getRole() == UserRole.SUPER_ADMIN || userToken.getRole() == UserRole.ADMIN) {
			// 只有影响行数大于0时才删除成功
			return this.baseMapper.deleteById(commentId) > 0;
		}
		return false;
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
		CompletableFuture.runAsync(() -> this.lambdaUpdate()
				.eq(Comment::getId, id)
				.setIncrBy(Comment::getLikes, isIncrement ? 1 : -1)
				.update(), globalTaskExecutor); // 使用自定义线程池执行异步任务
	}

	// 一次性将评论表的某一新闻的评论数同步到新闻表中
	@Override
	public void syncCommentCount() {
		this.baseMapper.syncCommentsCount();
		log.info("评论数已同步到新闻表中");
	}


	private IPage<CommentVo> listCommentsHandler(Long userId, Long newsId, QueryCommentDto queryDto) {
		// 设置分页默认参数
		setPageDefaults(queryDto);

		// 构建分页对象和查询条件
		IPage<Comment> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
		LambdaQueryWrapper<Comment> wrapper = buildQueryWrapper(userId, newsId, queryDto);

		try {
			// 执行分页查询
			IPage<Comment> commentPages = page(page, wrapper);
			List<Comment> comments = commentPages.getRecords();

			// 转换为CommentVo列表并获取用户ID集合
			List<CommentVo> commentVos = convertToCommentVos(comments);
			Set<Long> userIds = extractUserIds(commentVos);

			// 批量查询用户信息并建立映射
			Map<Long, UserProfile> profileMap = getUserProfileMap(userIds);

			// 填充用户信息到CommentVo
			populateUserInfo(commentVos, profileMap);

			// 构建分页结果
			return buildCommentVoPage(commentPages, commentVos);
		} catch (Exception e) {
			log.error("查询评论列表失败", e);
			return new Page<>();
		}
	}

	// 辅助方法分解
	private void setPageDefaults(QueryCommentDto queryDto) {
		if (queryDto.getPageNum() == null || queryDto.getPageNum() <= 0) {
			queryDto.setPageNum(1);
		}
		if (queryDto.getPageSize() == null || queryDto.getPageSize() <= 0) {
			queryDto.setPageSize(10);
		}
	}

	private LambdaQueryWrapper<Comment> buildQueryWrapper(Long userId, Long newsId, QueryCommentDto queryDto) {
		LambdaQueryWrapper<Comment> wrapper = Wrappers.lambdaQuery();
		if (userId != null) {
			wrapper.eq(Comment::getUserId, userId);
		} else if (newsId != null) {
			wrapper.eq(Comment::getNewsId, newsId);
		} else {
			throw new IllegalArgumentException("必须提供userId或newsId");
		}

		if (queryDto.getSortBy() == QueryCommentDto.SortBy.TIME) {
			wrapper.orderByDesc(Comment::getCreatedAt);
		} else if (queryDto.getSortBy() == QueryCommentDto.SortBy.LIKES) {
			wrapper.orderByDesc(Comment::getLikes);
		}
		return wrapper;
	}

	private List<CommentVo> convertToCommentVos(List<Comment> comments) {
		return comments.stream()
				.map(CommentVo::new)
				.collect(Collectors.toList());
	}

	private Set<Long> extractUserIds(List<CommentVo> commentVos) {
		return commentVos.stream()
				.map(vo -> vo.getCommentDetail().getUserId())
				.collect(Collectors.toSet());
	}

	private Map<Long, UserProfile> getUserProfileMap(Set<Long> userIds) {
		if (userIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return userProfileService.listUserProfiles(userIds).stream()
				.collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
	}

	private void populateUserInfo(List<CommentVo> commentVos, Map<Long, UserProfile> profileMap) {
		commentVos.forEach(vo -> {
			UserProfile profile = profileMap.get(vo.getCommentDetail().getUserId());
			if (profile != null) {
				vo.setNickname(profile.getNickname());
				vo.setAvatarUrl(profile.getAvatarUrl());
			} else {
				vo.setNickname("未知用户");
				vo.setAvatarUrl("");
				log.warn("用户ID:{} 的资料未找到", vo.getCommentDetail().getUserId());
			}
		});
	}

	private IPage<CommentVo> buildCommentVoPage(IPage<Comment> sourcePage, List<CommentVo> content) {
		Page<CommentVo> resultPage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
		resultPage.setRecords(content);
		return resultPage;
	}
}