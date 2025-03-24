package com.yubzhou.controller;

import com.yubzhou.common.Result;
import com.yubzhou.service.FileUploadService;
import com.yubzhou.service.FileUploadService.UploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/uploads")
@Slf4j
public class FileUploadController {

	private final FileUploadService fileUploadService;

	@Autowired
	public FileUploadController(FileUploadService fileUploadService) {
		this.fileUploadService = fileUploadService;
	}

	// 上传单张图片（异步）
	@PostMapping("/images")
	public CompletableFuture<Result<?>> handleUpload(@RequestParam("image") MultipartFile[] files) {
		long startTime = System.currentTimeMillis();
		// 接受所有图片类型
		Set<MediaType> allowedTypes = Set.of(new MediaType("image", "*"));
		CompletableFuture<UploadResult> future = fileUploadService.uploadImage(files, allowedTypes);
		long endTime = System.currentTimeMillis();
		log.info("upload images cost {} ms", endTime - startTime);
		return future.thenApply(Result::success); // 非阻塞返回
	}

	// 上传多张图片（异步）
	@PostMapping("/images/multi")
	public CompletableFuture<Result<?>> handleMultiUpload(@RequestParam("images") MultipartFile[] files) {
		long startTime = System.currentTimeMillis();
		// 接受所有图片类型
		Set<MediaType> allowedTypes = Set.of(new MediaType("image", "*"));
		CompletableFuture<UploadResult> future = fileUploadService.uploadImages(files, allowedTypes);
		long endTime = System.currentTimeMillis();
		log.info("upload images cost {} ms", endTime - startTime);
		return future.thenApply(Result::success); // 非阻塞返回
	}
}