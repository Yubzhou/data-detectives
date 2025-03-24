package com.yubzhou.service.impl;

import com.yubzhou.common.ReturnCode;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.properties.FileUploadProperties;
import com.yubzhou.service.FileUploadService;
import com.yubzhou.util.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

	private final FileUploadProperties fileUploadProperties;
	// 上传专用线程池
	private final ThreadPoolTaskExecutor uploadTaskExecutor;

	@Autowired
	public FileUploadServiceImpl(FileUploadProperties fileUploadProperties, ThreadPoolTaskExecutor uploadTaskExecutor) {
		this.fileUploadProperties = fileUploadProperties;
		this.uploadTaskExecutor = uploadTaskExecutor;
	}

	// 上传单个图片（异步）
	@Override
	public CompletableFuture<UploadResult> uploadImage(MultipartFile[] files, Set<MediaType> allowedTypes) {
		if (files != null && files.length != 1) {
			throw new BusinessException(ReturnCode.RC400.getCode(), "仅支持单个文件上传");
		}
		// 异步上传图片
		return CompletableFuture.supplyAsync(() -> uploadHandler(files, allowedTypes), uploadTaskExecutor);
	}

	// 上传多个图片（异步）
	@Override
	public CompletableFuture<UploadResult> uploadImages(MultipartFile[] files, Set<MediaType> allowedTypes) {
		// 异步上传图片
		return CompletableFuture.supplyAsync(() -> uploadHandler(files, allowedTypes), uploadTaskExecutor);
	}

	/**
	 * 文件上传处理器
	 *
	 * @param files        文件列表
	 * @param allowedTypes 允许的文件类型
	 * @return 上传结果
	 */
	private UploadResult uploadHandler(MultipartFile[] files, Set<MediaType> allowedTypes) {
		if (files == null || files.length == 0) {
			throw new BusinessException(ReturnCode.RC400.getCode(), "上传文件不能为空");
		}
		if (allowedTypes == null || allowedTypes.isEmpty()) {
			throw new BusinessException(ReturnCode.RC400.getCode(), "允许的文件类型不能为空");
		}

		// 筛选出空文件和非空的文件，空文件集合为true，非空文件集合为false
		Map<Boolean, List<MultipartFile>> filtered = this.filterFiles(files);
		List<MultipartFile> emptyFiles = filtered.get(true);
		List<MultipartFile> nonEmptyFiles = filtered.get(false);

		// 如果所有文件都为空，说明用户未选择文件，抛出异常
		if (nonEmptyFiles.isEmpty()) {
			throw new BusinessException(ReturnCode.RC400.getCode(), "未选择上传文件，请至少选择一个文件");
		}

		log.info("文件上传开始，数量: {}, 允许类型: {}", files.length, allowedTypes);
		long start = System.currentTimeMillis();

		// List<UploadResult.SuccessInfo> successFiles = new ArrayList<>();
		// List<UploadResult.ErrorInfo> errorFiles = new ArrayList<>();

		// 使用线程安全集合
		Queue<UploadResult.SuccessInfo> successFiles = new ConcurrentLinkedQueue<>();
		Queue<UploadResult.ErrorInfo> errorFiles = new ConcurrentLinkedQueue<>();

		// 获取图片上传目录（相对路径）
		String uploadDir = fileUploadProperties.getImage().getUploadDir();
		// 获取安全的绝对路径
		Path uploadPath = PathUtil.getExternalPath(uploadDir);

		try {
			// 创建多级目录
			Files.createDirectories(uploadPath);
		} catch (IOException e) {
			log.error("无法创建图片上传目录: {}", e.getMessage());
			throw new BusinessException(ReturnCode.RC500.getCode(), "系统内部错误: 无法创建上传目录");
		}

		// 并行处理每个文件
		List<CompletableFuture<Void>> futures = nonEmptyFiles.stream()
				.map(file -> CompletableFuture.runAsync(() -> {
					try {
						validateFile(file, allowedTypes);
						saveFile(file, uploadPath, successFiles);
					} catch (BusinessException e) {
						handleBusinessException(file, e, errorFiles);
					} catch (Exception e) {
						handleUnexpectedException(file, e, errorFiles);
					}
				}, uploadTaskExecutor)) // 使用上传专用线程池
				.toList();

		// 等待所有任务完成
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		// 如果存在空文件，则增加到失败文件中
		if (!emptyFiles.isEmpty()) {
			errorFiles.addAll(emptyFiles.stream()
					.map(f -> UploadResult.createErrorInfo(f.getOriginalFilename(), "空文件"))
					.toList());
		}

		long end = System.currentTimeMillis();
		log.info("文件上传完成，耗时: {}ms, 成功: {}, 失败: {}", end - start, successFiles.size(), errorFiles.size());

		return new UploadResult(
				files.length,
				successFiles.size(),
				errorFiles.size(),
				successFiles,
				errorFiles
		);
	}

	// 筛选出空文件和非空文件
	private Map<Boolean, List<MultipartFile>> filterFiles(MultipartFile[] files) {
		List<MultipartFile> emptyFiles = new ArrayList<>();
		List<MultipartFile> nonEmptyFiles = new ArrayList<>();

		if (files != null) {
			for (MultipartFile file : files) {
				if (file.isEmpty()) {
					emptyFiles.add(file);
				} else {
					nonEmptyFiles.add(file);
				}
			}
		}
		return Map.of(true, emptyFiles, false, nonEmptyFiles);
	}

	// 上传文件校验
	private void validateFile(MultipartFile file, Set<MediaType> allowedTypes) {
		if (!MediaTypeValidator.validate(file, allowedTypes)) {
			throw new BusinessException(ReturnCode.RC400.getCode(),
					"仅支持以下类型文件: " + MediaTypeDescriptions.getDescriptionsName(allowedTypes));
		}
	}

	// 保存文件
	private void saveFile(MultipartFile file, Path uploadPath, Queue<UploadResult.SuccessInfo> successFiles) throws IOException {
		// 生成唯一文件名
		String filename = generateSafeFilename(file);
		Path filePath = uploadPath.resolve(filename);
		file.transferTo(filePath);
		String accessUrl = fileUploadProperties.getImage().getAccessUrl();
		successFiles.add(UploadResult.createSuccessInfo(file.getOriginalFilename(), accessUrl + filename));
	}

	// 异常处理方法
	private void handleBusinessException(MultipartFile file, BusinessException e,
										 Queue<UploadResult.ErrorInfo> errorFiles) {
		String errorMsg = (e.getCode() == ReturnCode.RC500.getCode()) ?
				"系统内部错误: " + e.getMessage() : e.getMessage();
		errorFiles.add(UploadResult.createErrorInfo(file.getOriginalFilename(), errorMsg));
	}

	private void handleUnexpectedException(MultipartFile file, Exception e,
										   Queue<UploadResult.ErrorInfo> errorFiles) {
		log.error("文件上传未知异常: {}", file.getOriginalFilename(), e);
		errorFiles.add(UploadResult.createErrorInfo(file.getOriginalFilename(),
				"系统内部错误: " + e.getMessage()));
	}

	// 提取扩展名
	private static String getFileExtension(String filename) {
		String ext = "";
		int index = filename.lastIndexOf('.');
		if (index > 0) {
			ext = filename.substring(index);
			if (!ext.matches("\\.[a-zA-Z0-9]+")) {
				throw new BusinessException(ReturnCode.RC400.getCode(), "文件扩展名不合法");
			}
		}
		return ext;
	}

	/*
	 * 生成唯一文件名
	 * @param originalName 原始文件名
	 * @return 唯一文件名
	 */
	private String generateSafeFilename(MultipartFile file) {
		String ext = getFileExtension(file.getOriginalFilename());
		return UUID.randomUUID().toString().replace("-", "") + ext;
	}

	// 类型校验工具类
	public static class MediaTypeValidator {
		// public static boolean validate(MultipartFile file, Set<MediaType> allowedTypes) {
		// 	try {
		// 		MediaType actual = MediaType.parseMediaType(file.getContentType());
		// 		return allowedTypes.stream().anyMatch(allowed -> allowed.includes(actual));
		// 	} catch (Exception e) {
		// 		log.error("文件类型检测失败", e);
		// 		throw new BusinessException(ReturnCode.RC500.getCode(), "文件类型检测失败");
		// 	}
		// }

		public static boolean validate(MultipartFile file, Set<MediaType> allowedTypes) {
			// 检测真实 MIME 类型
			org.apache.tika.mime.MediaType realType;
			try {
				realType = FileTypeDetector.detectRealType(file);
				log.debug("检测到文件[{}]的真实类型: {}", file.getOriginalFilename(), realType);
			} catch (IOException e) {
				log.error("文件[{}]类型检测失败", file.getOriginalFilename(), e);
				throw new BusinessException(ReturnCode.RC500.getCode(), "文件类型检测失败");
			}

			// 将 tika 的 MediaType 转换为 spring 的 MediaType
			MediaType actual = MediaType.parseMediaType(realType.toString());

			// 校验扩展名与类型是否匹配（可选但推荐）
			String extension = getFileExtension(file.getOriginalFilename());
			// 去掉前缀的点
			if (!extension.isEmpty()) extension = extension.substring(1);
			if (!MediaTypeDescriptions.isExtensionValid(actual, extension)) {
				throw new BusinessException(ReturnCode.RC400.getCode(),
						"文件扩展名[." + extension + "]与文件实际类型[" + actual + "]不匹配");
			}

			return allowedTypes.stream().anyMatch(allowed -> allowed.includes(actual));
		}
	}
}