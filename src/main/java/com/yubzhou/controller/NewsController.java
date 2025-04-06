package com.yubzhou.controller;

import com.yubzhou.common.Result;
import com.yubzhou.common.UserActionEvent.ActionType;
import com.yubzhou.producer.NewsActionProducer;
import com.yubzhou.util.WebContextUtil;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@Validated // 启用参数验证
@Slf4j
public class NewsController {

	private final NewsActionProducer newsActionProducer;

	@Autowired
	public NewsController(NewsActionProducer newsActionProducer) {
		this.newsActionProducer = newsActionProducer;
	}

	// action参数：view、support、oppose、comment、favorite、unsupport、unoppose、uncomment、unfavorite
	@PostMapping("/{newsId:-?\\d+}/action")
	public Result<Void> action(@PathVariable @Min(value = 1, message = "newsId必须是正整数") Long newsId, // 限制newsId最小值为1
							   @RequestParam("action") String action) {
		Long userId = WebContextUtil.getCurrentUserId();
		ActionType actionType = ActionType.from(action);
		newsActionProducer.sendActionEvent(newsId, userId, actionType);
		return Result.successWithMessage("操作成功");
	}
}