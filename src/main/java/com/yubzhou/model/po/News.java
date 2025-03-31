package com.yubzhou.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yubzhou.common.UserActionEvent.ActionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("news") // mybatis-plus注解，指定对应的数据库表名
public class News {
	@TableId // 标记为主键
	private Long id; // 新闻ID

	private String title; // 新闻标题

	private String content; // 新闻内容

	private Integer views; // 浏览量

	private Integer supports; // 支持数

	private Integer opposes; // 反对数

	private Integer comments; // 评论数

	private Integer favorites; // 收藏数

	private String createdAt; // 创建时间

	private String updatedAt; // 更新时间

	public News(Integer views, Integer supports, Integer opposes, Integer comments, Integer favorites) {
		this.views = views;
		this.supports = supports;
		this.opposes = opposes;
		this.comments = comments;
		this.favorites = favorites;
	}

	public static News fromRedisHash(String key, Map<Object, Object> map) {
		News news = new News();
		news.setId(Long.parseLong(key));
		news.setViews((Integer) map.get(ActionType.VIEW.getField()));
		news.setSupports((Integer) map.get(ActionType.SUPPORT.getField()));
		news.setOpposes((Integer) map.get(ActionType.OPPOSE.getField()));
		news.setComments((Integer) map.get(ActionType.COMMENT.getField()));
		news.setFavorites((Integer) map.get(ActionType.FAVORITE.getField()));
		return news;
	}
}
