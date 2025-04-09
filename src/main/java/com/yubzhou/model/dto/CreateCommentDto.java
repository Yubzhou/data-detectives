package com.yubzhou.model.dto;

import com.yubzhou.model.po.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDto {

	@NotBlank(message = "评论内容不能为空")
	@Size(max = 500, message = "评论内容最长500字")
	private String comment;

	public Comment toEntity(Long userId, Long newsId) {
		Comment comment = new Comment();
		comment.setUserId(userId);
		comment.setNewsId(newsId);
		comment.setComment(this.comment);
		return comment;
	}
}