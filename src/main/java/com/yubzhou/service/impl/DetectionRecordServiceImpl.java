package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.mapper.DetectionRecordMapper;
import com.yubzhou.model.po.DetectionRecord;
import com.yubzhou.model.vo.DetectionStatsVo;
import com.yubzhou.service.DetectionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DetectionRecordServiceImpl
		extends ServiceImpl<DetectionRecordMapper, DetectionRecord>
		implements DetectionRecordService {

	/**
	 * 根据用户ID、时间范围、检测类型和检测结论筛选记录（分页）
	 *
	 * @param userId          用户ID（必选）
	 * @param days            时间范围（单位：天，例如 7 表示最近7天，可选）
	 * @param detectionType   检测类型（0: 高效，1: 高精度，可选）
	 * @param detectionResult 检测结论（true/false，可选）
	 * @param pageNum         当前页码（从1开始，必选）
	 * @param pageSize        每页数量（可选）
	 * @return 分页结果
	 */
	@Override
	public IPage<DetectionRecord> queryByUnionIndex(
			Long userId,
			Integer days,
			Integer detectionType,
			Boolean detectionResult,
			Integer pageNum,
			Integer pageSize) {

		pageNum = pageNum == null ? 1 : pageNum;
		pageSize = pageSize == null ? 10 : pageSize;

		LocalDateTime endTime = null, startTime = null;

		// 计算时间范围
		if (days != null) {
			endTime = LocalDateTime.now(); // 当前时间
			startTime = endTime.minusDays(days); // 开始时间 = 当前时间 - days天
		}

		// 创建分页对象
		Page<DetectionRecord> page = new Page<>(pageNum, pageSize);

		// 构建查询条件（使用 LambdaQueryWrapper）
		LambdaQueryWrapper<DetectionRecord> wrapper = Wrappers.lambdaQuery();

		wrapper.eq(DetectionRecord::getUserId, userId) // 必须要指定 user_id
				.ge(days != null, DetectionRecord::getCreatedAt, startTime)
				.le(days != null, DetectionRecord::getCreatedAt, endTime)
				.eq(detectionType != null, DetectionRecord::getDetectionType, detectionType)
				.eq(detectionResult != null, DetectionRecord::getDetectionResult, detectionResult)
				.orderByDesc(DetectionRecord::getCreatedAt);

		// 执行分页查询（使用联合索引字段）
		return page(page, wrapper);
	}

	@Override
	public DetectionStatsVo getDetectionStats(long userId) {
		return this.baseMapper.selectDetectionStats(userId);
	}


}
