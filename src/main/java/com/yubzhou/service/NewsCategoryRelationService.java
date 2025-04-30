package com.yubzhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.po.NewsCategoryRelation;

import java.util.List;
import java.util.Map;

public interface NewsCategoryRelationService extends IService<NewsCategoryRelation> {
	List<NewsCategoryRelation> getNewsCategoryList(Long newsId);

	void cacheNewsCategoryRelationToRedis(Long newsId);

	void cacheAllNewsCategoryRelationToRedis();

	Map<Long, List<String>> getNewsCategoryRelationMap(List<Long> newsIds);
	long getNewsCount(long categoryId);
}
