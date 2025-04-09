package com.yubzhou.model.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yubzhou.util.DateTimeUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("detection_records") // mybatis-plus注解，指定对应的数据库表名
public class DetectionRecord {

	@TableId // 标记为主键
	private Long id; // 检测记录ID

	private Long userId; // 用户ID

	private String content; // 检测内容

	private Boolean detectionResult; // 检测结果（false：虚假，true：真实）

	private BigDecimal reliability; // 检测可信度

	private String textAnalysis; // 文本描述分析结果

	private String commonSenseAnalysis; // 常识推理分析结果

	private Integer detectionType; // 检测类型（0: 高效率模式，1: 高精度模式）

	private String newsCategory; // 新闻分类（可选）

	private Boolean favorite; // 收藏状态（false：未收藏，true：已收藏）

	@JsonFormat(pattern = DateTimeUtil.LOCAL_DATE_TIME_NO_MILLIS_FORMAT)
	private LocalDateTime createdAt; // 创建时间
}