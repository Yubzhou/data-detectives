package com.yubzhou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yubzhou.common.Result;
import com.yubzhou.model.dto.CreateDetectionRecordDto;
import com.yubzhou.model.dto.QueryDetectionRecordDto;
import com.yubzhou.model.po.DetectionRecord;
import com.yubzhou.service.DetectionRecordService;
import com.yubzhou.util.WebContextUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detections")
@Validated
@Slf4j
public class DetectionController {

	private final DetectionRecordService detectionRecordService;

	@Autowired
	public DetectionController(DetectionRecordService detectionRecordService) {
		this.detectionRecordService = detectionRecordService;
	}

	@PostMapping("/create")
	public Result<Void> createDetection(@Valid @RequestBody
										@NotEmpty(message = "批量检测记录不能为空")
										List<CreateDetectionRecordDto> dtoList) {
		// 获取用户ID
		long userId = WebContextUtil.getCurrentUserId();
		// 校验通过后，调用服务层保存数据
		boolean success = createDetectionRecords(userId, dtoList);
		if (!success) return Result.fail("创建检测记录失败");
		return Result.successWithMessage("创建检测记录成功，数量: " + dtoList.size());
	}

	@PostMapping("/query")
	public Result<IPage<DetectionRecord>> queryDetectionRecords(@Valid @RequestBody QueryDetectionRecordDto dto) {

		log.info("Dto: {}", dto);
		long userId = WebContextUtil.getCurrentUserId();
		IPage<DetectionRecord> records = detectionRecordService.queryByUnionIndex(
				userId,
				dto.getDays(),
				dto.getDetectionType(),
				dto.getDetectionResult(),
				dto.getPageNum(),
				dto.getPageSize()
		);

		return Result.success(records);
	}

	// 将saveBatch提到controller层，防止service本类自己调用导致事务失效
	private boolean createDetectionRecords(final long userId, List<CreateDetectionRecordDto> dtoList) {
		List<DetectionRecord> records = dtoList.stream().map(dto -> dto.toEntity(userId)).toList();
		return detectionRecordService.saveBatch(records, detectionRecordService.DEFAULT_BATCH_SIZE);
	}
}