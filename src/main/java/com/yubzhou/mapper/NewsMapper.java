package com.yubzhou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yubzhou.model.po.News;
import com.yubzhou.service.NewsService.MinAndMaxId;
import org.apache.ibatis.annotations.Select;

public interface NewsMapper extends BaseMapper<News> {
	// 获取最小和最大ID
	@Select("SELECT MIN(id) AS minId, MAX(id) AS maxId FROM news")
	MinAndMaxId getMinAndMaxId();
}
