package com.yubzhou.controller;

import com.yubzhou.annotation.JwtIgnore;
import com.yubzhou.common.Result;
import com.yubzhou.properties.FileUploadProperties;
import com.yubzhou.service.FileUploadService;
import com.yubzhou.service.FileUploadService.UploadResult;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/uploads")
@Validated
@Slf4j
public class FileUploadController {

	private final FileUploadService fileUploadService;
	private final FileUploadProperties fileUploadProperties;

	@Autowired
	public FileUploadController(FileUploadService fileUploadService, FileUploadProperties fileUploadProperties) {
		this.fileUploadService = fileUploadService;
		this.fileUploadProperties = fileUploadProperties;
	}

	// 上传单张图片（异步）
	@PostMapping("/images")
	public CompletableFuture<Result<?>> handleUpload(@RequestParam("image")
													 @Size(min = 1, max = 1, message = "仅支持单个文件上传")
													 MultipartFile[] files) {
		long startTime = System.currentTimeMillis();
		// 接受所有图片类型
		Set<MediaType> allowedTypes = Set.of(new MediaType("image", "*"));
		// 获取图片上传临时目录（相对路径），防止用户更换头像时产生的未引用图片导致的存储浪费问题
		String relativeUploadDir = fileUploadProperties.getImage().getTempDir();
		CompletableFuture<UploadResult> future = fileUploadService.uploadImage(files, allowedTypes, relativeUploadDir);
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
		// 获取图片上传临时目录（相对路径），防止用户更换头像时产生的未引用图片导致的存储浪费问题
		String relativeUploadDir = fileUploadProperties.getImage().getTempDir();
		CompletableFuture<UploadResult> future = fileUploadService.uploadImages(files, allowedTypes, relativeUploadDir);
		long endTime = System.currentTimeMillis();
		log.info("upload images cost {} ms", endTime - startTime);
		return future.thenApply(Result::success); // 非阻塞返回
	}

	// 上传JSON文件（同步）
	// @PostMapping("/json")
	@JwtIgnore // 忽略JWT校验
	public CompletableFuture<Result<?>> handleJsonUpload(@RequestParam("json")
														 @Size(min = 1, max = 1, message = "仅支持单个文件上传")
														 MultipartFile[] files) {
		long startTime = System.currentTimeMillis();
		// 接受JSON文件类型
		Set<MediaType> allowedTypes = Set.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM);
		CompletableFuture<UploadResult> future = fileUploadService.uploadJson(files, allowedTypes);
		long endTime = System.currentTimeMillis();
		log.info("upload json cost {} ms", endTime - startTime);
		return future.thenApply(Result::success);
	}
}