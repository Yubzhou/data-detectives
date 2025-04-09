package com.yubzhou.model.vo;

import com.yubzhou.common.UserActionEvent.ActionType;
import com.yubzhou.model.po.News;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsVo {
	private News news; // 新闻详情
	private UserNewsAction actions; // 用户对新闻的操作
	private List<String> categories; // 新闻所属新闻分类

	public NewsVo(News news, UserNewsAction actions) {
		this.news = news;
		this.actions = actions;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserNewsAction {
		private Boolean support;
		private Boolean oppose;
		private Boolean favorite;

		public static UserNewsAction of(Map<String, Map<Object, Boolean>> userNewsActionMap, long newsId) {
			UserNewsAction actions = new UserNewsAction();
			actions.setSupport(userNewsActionMap.get(ActionType.SUPPORT.getField()).get(newsId));
			actions.setOppose(userNewsActionMap.get(ActionType.OPPOSE.getField()).get(newsId));
			actions.setFavorite(userNewsActionMap.get(ActionType.FAVORITE.getField()).get(newsId));
			return actions;
		}
	}
}
