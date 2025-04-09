package com.yubzhou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.dto.CreateDetectionRecordDto;
import com.yubzhou.model.po.DetectionRecord;

import java.util.List;

public interface DetectionRecordService extends IService<DetectionRecord> {

	boolean createDetectionRecord(final long userId, List<CreateDetectionRecordDto> dtoList);

	IPage<DetectionRecord> queryByUnionIndex(
			Long userId,
			Integer days,
			Integer detectionType,
			Boolean detectionResult,
			Integer pageNum,
			Integer pageSize
	);
}