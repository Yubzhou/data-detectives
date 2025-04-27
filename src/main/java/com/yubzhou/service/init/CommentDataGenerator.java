package com.yubzhou.service.init;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.common.MinAndMaxId;
import com.yubzhou.model.po.Comment;
import com.yubzhou.model.po.News;
import com.yubzhou.service.CommentService;
import com.yubzhou.service.NewsService;
import com.yubzhou.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentDataGenerator {

	private final UserService userService;
	private final NewsService newsService;
	private final CommentService commentService;
	private final ObjectMapper mapper;

	// 配置参数
	private static final int MIN_COMMENTS_PER_NEWS = 10; // 最小评论数量
	private static final int MAX_COMMENTS_PER_NEWS = 20; // 最大评论数量
	private static long USER_ID_MIN = 96L; // 最小用户ID
	private static long USER_ID_MAX; // 最大用户ID
	private static final int HIGH_LIKES_MIN = 10; // 最小高点赞数量
	private static final int HIGH_LIKES_MAX = 50; // 最大高点赞数量
	private static final int LOW_LIKES_MAX = 10; // 最大低点赞数量
	private static final double MAIN_OPINION_RATIO = 0.7; // 主观观点比例

	// 通过 @PostConstruct 初始化依赖项
	@PostConstruct
	private void init() {
		MinAndMaxId minAndMaxId = userService.getMinAndMaxId();
		// USER_ID_MIN = minAndMaxId.getMinId();
		USER_ID_MIN = 96L;
		USER_ID_MAX = minAndMaxId.getMaxId();

		log.info("User ID Range initialized: [{}-{}]", USER_ID_MIN, USER_ID_MAX);
	}

	private static final Random RANDOM = new Random();


	public void run(String... args) throws IOException {
		List<News> newsList = newsService.list();
		List<Comment> commentsToAdd = new ArrayList<>();

		Map<String, Map<String, Object>> commentMap = getCommentMapFromJson();

		for (News news : newsList) {
			// 跳过没有评论数据的新闻
			if (!commentMap.containsKey(news.getId().toString())) {
				log.info("新闻ID [{}] 没有评论数据", news.getId());
				continue;
			}

			// 生成当前新闻的评论数量
			int commentCount = MIN_COMMENTS_PER_NEWS + RANDOM.nextInt(MAX_COMMENTS_PER_NEWS - MIN_COMMENTS_PER_NEWS + 1);

			// 判断新闻倾向
			boolean isSupportDominant = news.getSupports() > news.getOpposes();

			List<String> comments = (List<String>) commentMap.get(news.getId().toString()).get("comments");
			int length = comments.size();

			for (int i = 0; i < length && i < commentCount; i++) {
				Comment comment = new Comment();
				comment.setNewsId(news.getId());
				comment.setUserId(getRandomUserId());
				comment.setComment(comments.get(i));
				comment.setLikes(generateLikes(isSupportDominant));
				comment.setCreatedAt(generateRandomTime(news.getCreatedAt()));

				commentsToAdd.add(comment);
			}
		}

		// commentsToAdd.forEach(System.out::println);

		// 批量插入（分批次防止过大）
		int batchSize = 500;
		commentService.saveBatch(commentsToAdd, batchSize);
		log.info("{}条评论数据已保存到数据库", commentsToAdd.size());

		// 一次性将评论表的某一新闻的评论数同步到新闻表中
		commentService.syncCommentCount();
		log.info("评论数已同步到新闻表中");
	}

	private Long getRandomUserId() {
		return USER_ID_MIN + RANDOM.nextLong(USER_ID_MAX - USER_ID_MIN + 1);
	}


	/**
	 * 根据随机数生成点赞数
	 * 此方法用于模拟生成一个用户或内容的点赞数，考虑到是否存在主导意见的情况
	 * 当支持主导意见时，生成的点赞数倾向于更高
	 *
	 * @param isSupportDominant 是否支持主导意见，用于决定点赞数的分布
	 * @return 生成的点赞数
	 */
	private Integer generateLikes(boolean isSupportDominant) {
		// 当支持主导意见时
		if (isSupportDominant) {
			// 如果随机数小于主导意见比例，生成高点赞数范围内的随机数
			return RANDOM.nextDouble() < MAIN_OPINION_RATIO ?
					HIGH_LIKES_MIN + RANDOM.nextInt(HIGH_LIKES_MAX - HIGH_LIKES_MIN + 1) :
					// 否则，生成低点赞数范围内的随机数
					RANDOM.nextInt(LOW_LIKES_MAX + 1);
		} else {
			// 当不支持主导意见时，逻辑相反
			return RANDOM.nextDouble() < MAIN_OPINION_RATIO ?
					RANDOM.nextInt(LOW_LIKES_MAX + 1) :
					HIGH_LIKES_MIN + RANDOM.nextInt(HIGH_LIKES_MAX - HIGH_LIKES_MIN + 1);
		}
	}

	private LocalDateTime generateRandomTime(LocalDateTime newsCreateTime) {
		// 评论时间在新闻创建后1小时到30天内随机
		int maxDays = 30;
		long minOffset = 3600; // 1小时
		long maxDaysInSeconds = (long) maxDays * 86400; // 30天

		LocalDateTime now = LocalDateTime.now();
		long maxPossibleOffset = Duration.between(newsCreateTime, now).getSeconds();

		// 如果新闻创建时间等于或晚于当前时间，抛出异常
		if (maxPossibleOffset <= 0) {
			throw new IllegalArgumentException("新闻创建时间必须早于当前时间");
		}

		// 计算允许的最大偏移量（不超过30天或当前时间差值）
		long maxAllowedOffset = Math.min(maxPossibleOffset, maxDaysInSeconds);

		// 调整最小偏移量，确保评论时间严格在创建时间之后
		if (maxAllowedOffset < minOffset) {
			minOffset = 1; // 至少1秒
		}

		// 生成随机偏移量
		long randomOffset = minOffset + (long) (RANDOM.nextDouble() * (maxAllowedOffset - minOffset));

		return newsCreateTime.plusSeconds(randomOffset);
	}

	private Map<String, Map<String, Object>> getCommentMapFromJson() throws IOException {
		InputStream is = CommentDataGenerator.class.getResourceAsStream("/data/news_comments.json");
		return mapper.readValue(is, new TypeReference<>() {
		});
	}

	// public static void main(String[] args) throws IOException {
	// 	InputStream is = CommentDataGenerator.class.getResourceAsStream("/data/news_comments.json");
	// 	ObjectMapper mapper = new ObjectMapper();
	// 	Map<String, Map<String, Object>> comments = mapper.readValue(is, new TypeReference<>() {
	// 	});
	// 	comments.forEach((key, value) -> {
	// 		List<String> commentsList = null;
	// 		if (value.get("comments") != null && value.get("comments") instanceof List) {
	// 			commentsList = (List<String>) value.get("comments");
	// 		}
	// 		System.out.println("newsId: " + key + ", comments: " + commentsList.get(0));
	// 	});
	// }
}