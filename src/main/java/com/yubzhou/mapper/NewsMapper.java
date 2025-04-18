package com.yubzhou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yubzhou.model.po.News;
import com.yubzhou.common.MinAndMaxId;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface NewsMapper extends BaseMapper<News> {
	// 获取最小和最大ID
	@Select("SELECT MIN(id) AS minId, MAX(id) AS maxId FROM news")
	MinAndMaxId getMinAndMaxId();

	// 全文搜索
	@Select("SELECT * FROM news WHERE MATCH(title) AGAINST(#{keyword}) LIMIT #{limit}")
	List<News> fulltextSearch(@Param("keyword") String keyword, @Param("limit") int limit);

	// 带相关性排序的版本（推荐）
	@Select("""
			SELECT *,
			MATCH(title) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AS relevance_score
			FROM news
			WHERE MATCH(title) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)
			ORDER BY relevance_score DESC
			LIMIT #{limit}
			""")
	List<News> fulltextSearchWithRelevance(@Param("keyword") String keyword, @Param("limit") int limit);
}
