package com.yubzhou.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionStatsVo {
	// 检测总次数
	private Integer totalDetections;

	// 最长连续检测天数
	private Integer maxContinuousDays;
}
