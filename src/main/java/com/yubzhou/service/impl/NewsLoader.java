package com.yubzhou.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.model.po.News;
import com.yubzhou.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class NewsLoader {
	private final NewsService newsService;
	private final ObjectMapper mapper;

	public void loadNewsData() throws IOException {

		InputStream is = getClass().getResourceAsStream("/data/news.json");

		List<News> newsList = mapper.readValue(is, new TypeReference<List<Map<String, String>>>() {
				})
				.stream()
				.map(this::convertToNews)
				.toList();

		// 分批次插入（每批100条）
		newsService.saveBatch(newsList, 100); // MP批量插入方法
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