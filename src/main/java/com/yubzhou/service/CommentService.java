package com.yubzhou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.dto.CreateCommentDto;
import com.yubzhou.model.dto.QueryCommentDto;
import com.yubzhou.model.po.Comment;

public interface CommentService extends IService<Comment> {
	IPage<Comment> listUserComments(Long userId, QueryCommentDto queryDto);

	IPage<Comment> listNewsComments(Long newsId, Long userId, QueryCommentDto queryDto);

	boolean createComment(Long userId, Long newsId, CreateCommentDto createDto);

	boolean deleteComment(Long commentId, Long userId);

	boolean likeComment(Long id, long userId);
}