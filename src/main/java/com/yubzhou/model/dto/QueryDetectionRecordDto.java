package com.yubzhou.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryDetectionRecordDto {

	@Min(value = 1, message = "时间范围不能小于1天") // 仅在 days 不为 null 时生效
	private Integer days; // 时间范围（单位：天，例如 7 表示最近7天，可选）

	@Min(value = 0, message = "检测类型只能是0或1（0表示高效率模式，1表示高精度模式）") // 仅在 detectionType 不为 null 时生效
	@Max(value = 1, message = "检测类型只能是0或1（0表示高效率模式，1表示高精度模式）") // 仅在 detectionType 不为 null 时生效
	private Integer detectionType; // 检测类型（0: 高效，1: 高精度，可选）

	private Boolean detectionResult; // 检测结论（true/false，可选）

	@NotNull(message = "当前页码不能为空")
	@Min(value = 1, message = "当前页码不能小于1") // 仅在 pageNum 不为 null 时生效
	private Integer pageNum = 1; // 当前页码（从1开始，必选）

	@Min(value = 1, message = "每页记录数不能小于1") // 仅在 pageSize 不为 null 时生效
	@Max(value = 25, message = "每页记录数不能超过25") // 仅在 pageSize 不为 null 时生效
	private Integer pageSize = 10; // 每页数量（默认为10，可选）
}