package com.yubzhou.service.init;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.model.po.News;
import com.yubzhou.model.po.NewsCategoryRelation;
import com.yubzhou.service.NewsCategoryRelationService;
import com.yubzhou.service.NewsCategoryService;
import com.yubzhou.service.NewsService;
import com.yubzhou.util.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

	// 读取指定目录，将全部文件名转为Map<Long, String>，其中键为扩展名前面的数字，值为文件名
	public void updateNewsCovers() throws IOException {
		String relativePath = "./default/news_cover";
		String prefix = "/default/news_cover/";
		Path externalPath = PathUtil.getExternalPath(relativePath);
		Map<Long, String> fileMap = FileUtil.processFiles(externalPath.toString(), prefix);
		// 将map转为News列表
		List<News> newsList = new ArrayList<>(fileMap.size());
		for (Map.Entry<Long, String> entry : fileMap.entrySet()) {
			News news = new News();
			news.setId(entry.getKey());
			news.setCoverUrl(entry.getValue());
			newsList.add(news);
		}
		newsService.updateBatchById(newsList, 100);
		log.info("插入新闻封面成功");
	}

	// 同步评论表的评论数到新闻表中
	public void updateNewsComments() {
		newsService.updateMetricsWithVersion(null);
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

	private static class FileUtil {
		public static Map<Long, String> processFiles(String directoryPath, String prefix) {
			File folder = new File(directoryPath);
			File[] files = folder.listFiles();

			log.warn("folder：{}", folder.getAbsolutePath());

			if (files != null) {
				return Arrays.stream(files)
						.filter(File::isFile)
						.collect(Collectors.toMap(
								FileUtil::parseFileInfo,
								file -> prefix + file.getName(),
								(existing, replacement) -> existing,
								LinkedHashMap::new
						));
			}
			return Collections.emptyMap();
		}

		private static Long parseFileInfo(File file) {
			String filename = file.getName();
			String[] parts = filename.split("\\.", 2);
			if (parts.length != 2) return null;
			return Long.parseLong(parts[0]);
		}

		public static void main(String[] args) {
			String relativePath = "./default/news_cover";
			String prefix = "/default/news_cover/";
			Path externalPath = PathUtil.getExternalPath(relativePath);
			Map<Long, String> fileMap = processFiles(externalPath.toString(), prefix);
			fileMap.forEach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));
		}
	}
}