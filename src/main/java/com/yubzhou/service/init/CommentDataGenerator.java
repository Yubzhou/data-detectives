package com.yubzhou.service.init;

import com.hankcs.hanlp.HanLP;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentDataGenerator {

	private final UserService userService;
	private final NewsService newsService;
	private final CommentService commentService;

	// 配置参数
	private static final int MIN_COMMENTS_PER_NEWS = 5; // 最小评论数量
	private static final int MAX_COMMENTS_PER_NEWS = 20; // 最大评论数量
	private static long USER_ID_MIN; // 最小用户ID
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
		USER_ID_MIN = 97L;
		USER_ID_MAX = minAndMaxId.getMaxId();

		log.info("User ID Range initialized: [{}-{}]", USER_ID_MIN, USER_ID_MAX);
	}

	private static final Random RANDOM = new Random();

	private static final String[] FACT_COMMENTS = {
			"这篇报道详细记录了%s的发展过程",
			"记者深入调查了%s事件的各个方面",
			"文中提到的%s数据值得进一步验证",
			"关于%s的现场报道非常生动具体",
			"该新闻在%s领域的专业度值得肯定",
			"对%s的后续影响分析不够充分",
			"如果能补充%s的对比数据会更好",
			"文中引用的%s案例很有代表性",
			"%s的统计方法需要更透明",
			"这篇追踪报道完善了%s的细节",
			"对%s的解读存在时间线混乱",
			"报道中%s的影像资料非常珍贵",
			"建议增加%s相关专家的访谈",
			"文中关于%s的背景介绍清晰",
			"%s的取证过程描述不够严谨",
			"该报道填补了%s领域的信息空白",
			"在%s问题上呈现了多方观点",
			"对%s的后续处理方案需要明确",
			"文中%s的对比图表直观易懂",
			"关于%s的法律依据分析透彻"
	};

	private static final String[] EMOTION_COMMENTS = {
			"看到%s的报道令人振奋！",
			"为%s的进步感到由衷高兴",
			"对文中描述的%s深感忧虑",
			"这种处理%s的方式让人失望",
			"期待%s问题能早日解决",
			"看到%s的现状非常痛心",
			"为坚守在%s岗位的人点赞",
			"这种%s乱象必须得到整治",
			"关于%s的真相令人震惊",
			"支持对%s问题的严肃处理",
			"这种对待%s的态度不可取",
			"看到%s的创新成果很自豪",
			"对%s的漠视让人气愤",
			"这种解决%s的方式很智慧",
			"关于%s的谎言必须揭穿"
	};

	private static final String[] CRITICAL_COMMENTS = {
			"文中关于%s的结论是否草率？",
			"建议复核%s的数据来源",
			"对%s的处理方式是否合法？",
			"为什么回避%s的关键问题？",
			"请公布%s的完整证据链",
			"%s的决策过程需要透明化",
			"是否存在%s的利益输送？",
			"为什么忽视%s的替代方案？",
			"请解释%s的经费使用明细",
			"%s的标准制定是否合规？",
			"为何延迟公布%s的真相？",
			"对%s的监管是否存在漏洞？",
			"%s的责任认定是否公正？",
			"请说明%s的选拔流程",
			"这种处理%s是否双重标准？"
	};

	private static final String[] EXTENSION_COMMENTS = {
			"%s问题让我联想到...",
			"如果从%s角度看这个问题...",
			"历史上类似的%s事件...",
			"国际社会如何处理%s问题？",
			"新技术如何应用于%s领域？",
			"从法律层面解读%s...",
			"教育系统应该如何应对%s...",
			"%s对下一代的影响是...",
			"从经济学角度分析%s...",
			"人工智能如何助力解决%s...",
			"传统文化视角下的%s...",
			"环境保护与%s如何平衡？",
			"社会治理创新在%s中的应用",
			"从心理学角度理解%s现象",
			"科技创新如何突破%s瓶颈"
	};

	private static final String[] PREDICT_COMMENTS = {
			"预计%s将在3年内...",
			"如果不解决%s问题，未来可能...",
			"%s技术有望在2025年...",
			"这种处理方式将导致%s...",
			"%s领域即将迎来爆发期",
			"专家预测%s成本将下降...",
			"这种趋势会加速%s的...",
			"国际局势可能影响%s...",
			"新政策将重塑%s格局",
			"资本市场对%s的反应可能..."
	};

	public void run(String... args) {
		List<News> newsList = newsService.list();
		List<Comment> commentsToAdd = new ArrayList<>();

		for (News news : newsList) {
			// 生成当前新闻的评论数量
			int commentCount = MIN_COMMENTS_PER_NEWS + RANDOM.nextInt(MAX_COMMENTS_PER_NEWS - MIN_COMMENTS_PER_NEWS + 1);

			// 判断新闻倾向
			boolean isSupportDominant = news.getSupports() > news.getOpposes();

			for (int i = 0; i < commentCount; i++) {
				Comment comment = new Comment();
				comment.setNewsId(news.getId());
				comment.setUserId(getRandomUserId());
				// comment.setComment(generateCommentText(news.getTitle()));
				comment.setComment(generateSmartComment(news.getTitle()));
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

	// 修改后的评论生成方法
	private String generateCommentText(String newsTitle) {
		// 从新闻标题中提取关键词（至少2个字符）
		List<String> titleKeywords = splitTitleKeywords(newsTitle);

		// 混合评论模板（包含相关和通用类型）
		if (!titleKeywords.isEmpty() && RANDOM.nextDouble() < 0.7) {
			// 70%概率生成与标题相关的评论
			return buildTitleRelatedComment(titleKeywords);
		} else {
			// 30%概率生成通用评论
			return buildGenericComment();
		}
	}

	private String generateSmartComment(String newsTitle) {
		// 智能分配模板类型权重
		double[] probabilities = {0.25, 0.2, 0.2, 0.2, 0.15};
		double roll = RANDOM.nextDouble();

		// 从新闻标题中提取关键词（至少2个字符）
		List<String> titleKeywords = splitTitleKeywords(newsTitle);

		// 混合评论模板（包含相关和通用类型）
		if (!titleKeywords.isEmpty() && RANDOM.nextDouble() < 0.7) {
			// 70%概率生成与标题相关的评论
			String keyword = titleKeywords.get(RANDOM.nextInt(titleKeywords.size()));
			if (roll < probabilities[0]) {
				return buildComment(FACT_COMMENTS, keyword);
			} else if (roll < probabilities[0] + probabilities[1]) {
				return buildComment(EMOTION_COMMENTS, keyword);
			} else if (roll < probabilities[0] + probabilities[1] + probabilities[2]) {
				return buildComment(CRITICAL_COMMENTS, keyword);
			} else if (roll < probabilities[0] + probabilities[1] + probabilities[2] + probabilities[3]) {
				return buildComment(EXTENSION_COMMENTS, keyword);
			} else {
				return buildComment(PREDICT_COMMENTS, keyword);
			}
		} else {
			// 30%概率生成通用评论
			return buildGenericComment();
		}
	}

	private String buildComment(String[] templates, String keyword) {
		return String.format(templates[RANDOM.nextInt(templates.length)], keyword);
	}


	// // 标题关键词提取（简易中文分词）
	// private List<String> splitTitleKeywords(String title) {
	// 	List<String> keywords = new ArrayList<>();
	// 	int len = title.length();
	//
	// 	// 提取2-3字组合的关键词
	// 	for (int i = 0; i < len - 1; i++) {
	// 		if (i < len - 2) {
	// 			keywords.add(title.substring(i, i + 3)); // 三字词
	// 		}
	// 		keywords.add(title.substring(i, i + 2)); // 两字词
	// 	}
	//
	// 	return keywords.stream()
	// 			.distinct()
	// 			.toList();
	// }

	// 改进后的分词方法
	private List<String> splitTitleKeywords(String title) {
		return HanLP.segment(title).stream()
				.map(term -> term.word)
				.filter(word -> word.length() >= 2)
				.toList();
	}

	// 构建标题相关评论
	private String buildTitleRelatedComment(List<String> keywords) {
		String keyword = keywords.get(RANDOM.nextInt(keywords.size()));

		String[] relatedTemplates = {
				"关于%s的报道非常客观！",
				"这篇新闻在%s方面分析到位！",
				"不认同%s部分的内容，存在偏颇。",
				"%s的情况需要更多数据支持",
				"作者对%s的分析很有见地",
				"希望能更全面报道%s相关内容"
		};

		return String.format(relatedTemplates[RANDOM.nextInt(relatedTemplates.length)], keyword);
	}

	// 构建通用评论
	private String buildGenericComment() {
		String[] genericComments = {
				"这篇报道提供了新的视角！",
				"数据来源需要更透明些",
				"希望看到更多后续跟踪报道",
				"作者的观点很有启发性",
				"论证过程有些仓促了",
				"需要更多证据支持结论",
				"这个议题值得深入探讨"
		};

		return genericComments[RANDOM.nextInt(genericComments.length)];
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
		long maxOffset = (long) maxDays * 86400; // 30天

		long randomOffset = minOffset + (long) (RANDOM.nextDouble() * (maxOffset - minOffset));
		return newsCreateTime.plusSeconds(randomOffset);
	}
}