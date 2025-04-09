package com.yubzhou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yubzhou.common.Result;
import com.yubzhou.model.dto.CreateDetectionRecordDto;
import com.yubzhou.model.po.DetectionRecord;
import com.yubzhou.service.DetectionRecordService;
import com.yubzhou.util.WebContextUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detections")
@Validated
public class DetectionController {

	private final DetectionRecordService detectionRecordService;

	@Autowired
	public DetectionController(DetectionRecordService detectionRecordService) {
		this.detectionRecordService = detectionRecordService;
	}

	@PostMapping("/create")
	public Result<Void> createDetection(@Valid @RequestBody CreateDetectionRecordDto dto) {
		// 获取用户ID
		long userId = WebContextUtil.getCurrentUserId();
		// 校验通过后，调用服务层保存数据
		boolean success = detectionRecordService.createDetectionRecord(userId, dto);
		if (!success) return Result.fail("创建检测记录失败");
		return Result.successWithMessage("创建检测记录成功");
	}

	@GetMapping("/query")
	public Result<IPage<DetectionRecord>> queryDetectionRecords(
			@RequestParam(required = false) Integer days,
			@RequestParam(required = false)
			@Min(value = 0, message = "检测类型只能是0或1")
			@Max(value = 1, message = "检测类型只能是0或1")
			Integer detectionType,
			@RequestParam(required = false) Boolean detectionResult,
			@RequestParam(defaultValue = "1") Integer pageNum, // 必须传
			@RequestParam(defaultValue = "10", required = false)
			@Max(value = 25, message = "每页最大记录数不能超过25")
			Integer pageSize) {

		long userId = WebContextUtil.getCurrentUserId();
		IPage<DetectionRecord> records = detectionRecordService.queryByUnionIndex(
				userId,
				days,
				detectionType,
				detectionResult,
				pageNum,
				pageSize);

		return Result.success(records);
	}


}