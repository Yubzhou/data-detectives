package com.yubzhou.controller;

import com.yubzhou.common.Result;
import com.yubzhou.model.vo.DetectionStatsVo;
import com.yubzhou.service.DetectionRecordService;
import com.yubzhou.util.WebContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

	private final DetectionRecordService detectionRecordService;

	// 聚合统计接口：新闻检测总次数、检测准确率
	@GetMapping("/detection")
	public Result<DetectionStatsVo> DetectionStatistics() {
		// 获取当前用户ID
		long userId = WebContextUtil.getCurrentUserId();
		// 获取新闻检测总次数、新闻检测准确率
		DetectionStatsVo detectionStats = detectionRecordService.getDetectionStats(userId);
		return Result.success(detectionStats);
	}

}
