package com.yubzhou.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementVo {
	// 检测总次数
	private Integer totalDetections;

	// 最长连续检测天数
	private Integer maxContinuousDays;

	// 用户评论总数
	Integer totalComments;

	// 用户检测记录中真新闻占比
	Double trueNewsDetectionRatio;
}
