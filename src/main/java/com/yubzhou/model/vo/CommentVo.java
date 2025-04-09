package com.yubzhou.model.vo;

import com.yubzhou.model.po.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentVo {

	private Comment commentDetail; // 评论对象

	private String nickname; // 用户昵称

	private String avatarUrl; // 用户头像

	public CommentVo(Comment comment) {
		this.commentDetail = comment;
	}
}
