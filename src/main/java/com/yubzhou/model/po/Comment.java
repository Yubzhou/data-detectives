package com.yubzhou.model.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yubzhou.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("comments") // mybatis-plus注解，指定对应的数据库表名
public class Comment {
	@TableId // 标记为主键
	private Long id;

	private Long userId;

	private Long newsId;

	private String comment;

	private Integer likes;

	@JsonFormat(pattern = DateTimeUtil.LOCAL_DATE_TIME_NO_MILLIS_FORMAT)
	private LocalDateTime createdAt;

	@TableField(exist = false) // 表示该字段不是数据库表中的字段，只用于前端返回
	private Boolean isLiked;
}