package com.yubzhou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yubzhou.model.dto.CreateDetectionRecordDto;
import com.yubzhou.model.po.DetectionRecord;
import com.yubzhou.model.vo.AchievementVo;

import java.util.List;

public interface DetectionRecordService extends IService<DetectionRecord> {

	IPage<DetectionRecord> queryByUnionIndex(
			Long userId,
			Integer days,
			Integer detectionType,
			Boolean detectionResult,
			Integer pageNum,
			Integer pageSize
	);

	Integer getMaxContinuousDaysFromDB(long userId);

	Long getMaxContinuousDaysFromCache(long userId);

	AchievementVo getAchievement(long userId);

	boolean processDetection(Long userId, List<CreateDetectionRecordDto> dtoList);
}