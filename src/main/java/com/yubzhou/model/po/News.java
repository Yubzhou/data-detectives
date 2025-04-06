package com.yubzhou.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubzhou.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

	@JsonIgnore // 忽略该字段，不进行序列化
	private Integer version; // 版本号

	@JsonFormat(pattern = DateTimeUtil.LOCAL_DATE_TIME_NO_MILLIS_FORMAT)
	private LocalDateTime createdAt; // 创建时间

	@JsonFormat(pattern = DateTimeUtil.LOCAL_DATE_TIME_NO_MILLIS_FORMAT)
	@JsonIgnore // 忽略该字段，不进行序列化
	private LocalDateTime updatedAt; // 更新时间

	public News(Long userId) {
		this.id = userId;
	}

	// 将新闻对象转换为Map（redis存储所需）
	public Map<String, Object> toRedisMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("id", this.getId());
		map.put("title", this.getTitle());
		map.put("content", this.getContent());
		map.put("views", this.getViews());
		map.put("supports", this.getSupports());
		map.put("opposes", this.getOpposes());
		map.put("comments", this.getComments());
		map.put("favorites", this.getFavorites());
		map.put("version", this.getVersion()); // 乐观锁，版本号
		map.put("createdAt", this.getCreatedAt());
		// map.put("updatedAt", this.getUpdatedAt());
		return map;
	}

	// 将Map转换为新闻对象（从redis中获取的）
	public static News fromRedisMap(Map<Object, Object> map) {
		// 从Map中创建新闻对象
		News news = new News();
		news.setId(Long.parseLong(map.get("id").toString()));
		news.setTitle((String) map.get("title"));
		news.setContent((String) map.get("content"));
		news.setViews((Integer) map.get("views"));
		news.setSupports((Integer) map.get("supports"));
		news.setOpposes((Integer) map.get("opposes"));
		news.setComments((Integer) map.get("comments"));
		news.setFavorites((Integer) map.get("favorites"));
		news.setVersion((Integer) map.get("version"));
		news.setCreatedAt(DateTimeUtil.parseLocalDateTimeNoMillis(map.get("createdAt").toString()));
		// news.setUpdatedAt(DateTimeUtil.parseLocalDateTimeNoMillis(map.get("updatedAt").toString()));
		return news;
	}
}
