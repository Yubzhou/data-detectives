package com.yubzhou.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsVo {
	private Long id; // 新闻ID

	private String title; // 新闻标题

	private String content; // 新闻内容

	private Integer views; // 浏览量

	private Integer supports; // 支持数

	private Integer opposes; // 反对数

	private Integer comments; // 评论数

	private Integer favorites; // 收藏数

	private String createdAt; // 创建时间

	private Set<String> categories; // 新闻分类
}