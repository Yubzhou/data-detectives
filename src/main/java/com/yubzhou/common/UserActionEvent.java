package com.yubzhou.common;

import com.yubzhou.exception.BusinessException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class UserActionEvent {
	private Long newsId;     // 新闻ID
	private Long userId;     // 用户ID
	private ActionType action;     // 行为类型：VIEW, SUPPORT, OPPOSE, COMMENT, FAVORITE
	private Long timestamp; // 事件时间戳

	@Getter
	@RequiredArgsConstructor
	public enum ActionType {
		// 其中SUPPORT和OPPOSE的权重相同，且它们之间有且仅能同时存在一个，所以全部权重加一起为10
		VIEW(1.5, "views"), // 浏览
		SUPPORT(2.5, "supports"), // 支持
		OPPOSE(SUPPORT.weight, "opposes"), // 反对
		COMMENT(3.5, "comments"), // 评论
		FAVORITE(2.5, "favorites"), // 收藏
		UNSUPPORT(SUPPORT.weight, "unsupports"), // 取消支持
		UNOPPOSE(OPPOSE.weight, "unopposes"), // 取消反对
		UNCOMMENT(COMMENT.weight, "uncomments"), // 取消评论
		UNFAVORITE(FAVORITE.weight, "unfavorites"), // 取消收藏
		;

		// 权重：用来计算动态计算新闻热度
		private final double weight;
		private final String field;

		// 小写格式的行为类型
		public static final Set<String> ACTION_TYPES =
				Stream.of(ActionType.values()).map(value -> value.name().toLowerCase()).collect(Collectors.toSet());

		public static ActionType from(String actionType) {
			ActionType type = null;
			try {
				type = ActionType.valueOf(actionType.toUpperCase());
			} catch (Exception e) {
				log.error("无效的动作类型: {}", actionType);
				String message = String.format("无效的动作类型: %s，有效的动作类型有：%s", actionType, ACTION_TYPES);
				throw new BusinessException(ReturnCode.RC400.getCode(), message);
			}
			return type;
		}

		public static double calculateHotScore(Map<Object, Object> metrics) {
			int views = (int) metrics.getOrDefault(ActionType.VIEW.getField(), 0);
			int supports = (int) metrics.getOrDefault(ActionType.SUPPORT.getField(), 0);
			int opposes = (int) metrics.getOrDefault(ActionType.OPPOSE.getField(), 0);
			int comments = (int) metrics.getOrDefault(ActionType.COMMENT.getField(), 0);
			int favorites = (int) metrics.getOrDefault(ActionType.FAVORITE.getField(), 0);
			return Math.log(views + 1) * VIEW.getWeight()
					+ (supports + opposes) * SUPPORT.getWeight()
					+ comments * COMMENT.getWeight()
					+ favorites * FAVORITE.getWeight();
		}
	}
}