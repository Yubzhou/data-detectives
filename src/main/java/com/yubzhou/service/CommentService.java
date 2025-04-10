package com.yubzhou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.dto.CreateCommentDto;
import com.yubzhou.model.dto.QueryCommentDto;
import com.yubzhou.model.po.Comment;
import com.yubzhou.model.vo.CommentVo;

public interface CommentService extends IService<Comment> {
	IPage<CommentVo> listUserComments(Long userId, QueryCommentDto queryDto);

	IPage<CommentVo> listNewsComments(Long newsId, Long userId, QueryCommentDto queryDto);

	Comment createComment(Long userId, Long newsId, CreateCommentDto createDto);

	boolean deleteComment(Long commentId, Long userId);

	boolean likeComment(Long id, long userId);

	void syncCommentCount();
}