package com.yubzhou.controller;

import com.yubzhou.common.ReturnCode;
import com.yubzhou.common.UserActionEvent;
import com.yubzhou.common.UserActionEvent.ActionType;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.producer.NewsActionProducer;
import com.yubzhou.util.WebContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news/{newsId}")
@Slf4j
public class NewsActionController {

	private final NewsActionProducer newsActionProducer;

	@Autowired
	public NewsActionController(NewsActionProducer newsActionProducer) {
		this.newsActionProducer = newsActionProducer;
	}

	// action参数：view、support、oppose、comment、favorite
	@PostMapping("/action")
	public ResponseEntity<?> action(@PathVariable Long newsId, String action) {
		Long userId = WebContextUtil.getCurrentUserId();
		ActionType actionType = ActionType.from(action);
		newsActionProducer.sendActionEvent(newsId, userId, actionType);
		return ResponseEntity.ok().build();
	}
}