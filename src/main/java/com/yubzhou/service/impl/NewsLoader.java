package com.yubzhou.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.model.po.News;
import com.yubzhou.model.po.NewsCategoryRelation;
import com.yubzhou.service.NewsCategoryRelationService;
import com.yubzhou.service.NewsCategoryService;
import com.yubzhou.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class NewsLoader {
	private final NewsService newsService;
	private final ObjectMapper mapper;
	private final NewsCategoryService newsCategoryService;
	private final NewsCategoryRelationService newsCategoryRelationService;

	public void insertNewsData() throws IOException {

		InputStream is = getClass().getResourceAsStream("/data/news.json");

		List<News> newsList = mapper.readValue(is, new TypeReference<List<Map<String, String>>>() {
				})
				.stream()
				.map(this::convertToNews)
				.toList();

		// 分批次插入（每批100条）
		newsService.saveBatch(newsList, 100); // MP批量插入方法
	}

	public void insertNewsCategoryRelations() throws IOException {
		InputStream is = getClass().getResourceAsStream("/data/news_category_relations.json");

		List<Map<String, Object>> relations = mapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {
		});

		long newsId = 70;
		List<NewsCategoryRelation> insertData = new ArrayList<>();
		for (Map<String, Object> relation : relations) {
			List<String> categoryNames = (List<String>) relation.get("categoryNames");
			for (String categoryName : categoryNames) {
				insertData.add(new NewsCategoryRelation(newsId, newsCategoryService.getNewsCategoryId(categoryName)));
			}
			newsId++;
		}

		newsCategoryRelationService.saveBatch(insertData, 100);
	}

	private News convertToNews(Map<String, String> item) {
		Random rand = new Random();
		LocalDateTime now = LocalDateTime.now().minusDays(rand.nextInt(30));

		News news = new News();
		news.setTitle(item.get("title"));
		news.setContent(item.get("content"));
		news.setViews(rand.nextInt(9901) + 100);
		news.setSupports(rand.nextInt(1000));
		news.setOpposes(rand.nextInt(100));
		news.setFavorites(rand.nextInt(500));
		news.setVersion(0);
		news.setCreatedAt(now);
		news.setUpdatedAt(now);

		return news;
	}
}