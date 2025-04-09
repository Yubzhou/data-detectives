package com.yubzhou.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("categories") // mybatis-plus注解，指定对应的数据库表名
public class NewsCategory {
	@TableId // 标记为主键
	private Long id;
	private String name;

	public static NewsCategory of(Long id, String name) {
		return new NewsCategory(id, name);
	}
}
