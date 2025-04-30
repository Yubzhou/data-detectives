package com.yubzhou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.po.DetectionRecord;
import com.yubzhou.model.vo.DetectionStatsVo;

public interface DetectionRecordService extends IService<DetectionRecord> {

	IPage<DetectionRecord> queryByUnionIndex(
			Long userId,
			Integer days,
			Integer detectionType,
			Boolean detectionResult,
			Integer pageNum,
			Integer pageSize
	);

	DetectionStatsVo getDetectionStats(long userId);
}