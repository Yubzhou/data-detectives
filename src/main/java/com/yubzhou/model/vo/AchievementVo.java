package com.yubzhou.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementVo {
	// 年度达人阈值（即yearToDateDetectionDays >= yearToDateTotalDays * ANNUAL_MASTER_THRESHOLD）
	private static final Double ANNUAL_MASTER_THRESHOLD = 0.8;

	// 用户新闻检测总次数
	private Long totalDetections;

	// 用户最长连续检测天数
	private Long maxContinuousDays;

	// 用户评论总数
	private Long totalComments;

	// 用户检测记录结果为真新闻的数量
	@JsonIgnore
	private Long trueNewsCount;

	// 用户今年到现在的检测天数（即有多少天检测过）
	@JsonIgnore
	private Integer totalDetectionDays;

	// 用户检测记录中真新闻占比
	public Double getTrueNewsDetectionRatio() {
		return trueNewsCount / (double) totalDetections;
	}

	// 是否为年度达人（如果检测天数 >= 今年到现在总天数 * 0.8，则为年度达人）
	public Boolean getIsAnnualMaster() {
		return totalDetectionDays >= getYearToDateTotalDays() * ANNUAL_MASTER_THRESHOLD;
	}

	// 计算今年到现在的总自然天数
	private static Integer getYearToDateTotalDays() {
		return LocalDate.now().getDayOfYear();
	}
}
