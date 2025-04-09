package com.yubzhou.model.dto;

import com.yubzhou.model.po.DetectionRecord;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDetectionRecordDto {

	// 检测内容（必填，最大65535字符）
	@NotBlank(message = "检测内容不能为空")
	@Size(max = 65535, message = "检测内容长度不能超过65535字符")
	private String content;

	// 检测结论（必填）
	@NotNull(message = "检测结论不能为空")
	private Boolean detectionResult;

	// 可信度（必填，0 <= 可信度 <= 100）
	@NotNull(message = "可信度不能为空")
	@DecimalMin(value = "0.0", message = "可信度不能小于0")
	@DecimalMax(value = "100.0", message = "可信度不能超过100")
	private BigDecimal reliability;

	// 文本分析结果（必填，最大65535字符）
	@NotBlank(message = "文本分析结果不能为空")
	@Size(max = 65535, message = "文本分析结果长度不能超过65535字符")
	private String textAnalysis;

	// 常识推理分析结果（必填，最大65535字符）
	@NotBlank(message = "常识推理分析结果不能为空")
	@Size(max = 65535, message = "常识推理分析结果长度不能超过65535字符")
	private String commonSenseAnalysis;

	// 检测类型（必填，0或1）
	@NotNull(message = "检测类型不能为空")
	@Min(value = 0, message = "检测类型只能是0或1")
	@Max(value = 1, message = "检测类型只能是0或1")
	private Integer detectionType;

	@Size(max = 25, message = "新闻类别长度不能超过25字符")
	private String newsCategory; // 新闻类别（可选，最大25字符）

	public DetectionRecord toEntity(long userId) {
		DetectionRecord detectionRecord = new DetectionRecord();
		detectionRecord.setUserId(userId);
		detectionRecord.setContent(this.content);
		detectionRecord.setDetectionResult(this.detectionResult);
		detectionRecord.setReliability(this.reliability);
		detectionRecord.setTextAnalysis(this.textAnalysis);
		detectionRecord.setCommonSenseAnalysis(this.commonSenseAnalysis);
		detectionRecord.setDetectionType(this.detectionType);
		detectionRecord.setNewsCategory(this.newsCategory);
		return detectionRecord;
	}
}