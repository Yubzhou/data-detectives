package com.yubzhou.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetectionStats {
	// 历史最长连续检测天数
	private Long maxContinuousDays;

	// 当前连续检测天数
	private Long currentContinuousDays;

	// 最后一次检测日期
	private LocalDate lastDetectionDate;

	// 创建包含默认值的UserDetectionStats对象
	public static UserDetectionStats createDefault() {
		UserDetectionStats stats = new UserDetectionStats();
		stats.setMaxContinuousDays(0L);
		stats.setCurrentContinuousDays(0L);
		// stats.setLastDetectionDate(null); // 无需设置，因为类字段默认为null
		return stats;
	}
}