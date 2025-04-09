package com.yubzhou.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("news_categories") // mybatis-plus注解，指定对应的数据库表名
public class NewsCategoryRelation {
	private Long newsId;
	private Long categoryId;
}
