package com.yubzhou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.dto.CreateDetectionRecordDto;
import com.yubzhou.model.po.DetectionRecord;

public interface DetectionRecordService extends IService<DetectionRecord> {

	boolean createDetectionRecord(long userId, CreateDetectionRecordDto dto);

	IPage<DetectionRecord> queryByUnionIndex(
			Long userId,
			Integer days,
			Integer detectionType,
			Boolean detectionResult,
			Integer pageNum,
			Integer pageSize
	);
}