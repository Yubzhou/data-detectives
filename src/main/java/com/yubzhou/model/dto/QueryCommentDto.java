package com.yubzhou.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryCommentDto {
	private SortBy sortBy = SortBy.TIME;  // 排序字段：time（默认）/likes

	@NotNull
	@Min(value = 1, message = "当前页码不能小于1") // 仅在 pageNum 不为 null 时生效
	private Integer pageNum = 1;

	@Min(value = 1, message = "每页记录数不能小于1") // 仅在 pageSize 不为 null 时生效
	@Max(value = 25, message = "每页记录数不能超过25") // 仅在 pageSize 不为 null 时生效
	private Integer pageSize = 10;

	public enum SortBy {
		TIME, // 时间
		LIKES, // 点赞数
		;
	}
}