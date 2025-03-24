// package com.yubzhou.service.impl;
//
// import com.yubzhou.common.ReturnCode;
// import com.yubzhou.exception.BusinessException;
// import com.yubzhou.properties.FileUploadProperties;
// import com.yubzhou.service.FileUploadService;
// import com.yubzhou.util.PathUtil;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;
//
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.*;
//
// @Service
// @Slf4j
// public class FileUploadServiceImpl implements FileUploadService {
//
// 	private final FileUploadProperties fileUploadProperties;
//
// 	@Autowired
// 	public FileUploadServiceImpl(FileUploadProperties fileUploadProperties) {
// 		this.fileUploadProperties = fileUploadProperties;
// 	}
//
// 	// 上传单个图片
// 	@Override
// 	public Map<String, Object> uploadImage(MultipartFile file, Set<MediaType> allowedTypes) {
// 		MultipartFile[] files = {file};
// 		return this.uploadImages(files, allowedTypes);
// 	}
//
// 	// 上传多个图片
// 	@Override
// 	public Map<String, Object> uploadImages(MultipartFile[] files, Set<MediaType> allowedTypes) {
// 		return this.uploadHandler(files, allowedTypes);
// 	}
//
// 	/**
// 	 * 文件上传处理器
// 	 *
// 	 * @param files        文件列表
// 	 * @param allowedTypes 允许的文件类型
// 	 * @return 上传结果
// 	 */
// 	private Map<String, Object> uploadHandler(MultipartFile[] files, Set<MediaType> allowedTypes) {
// 		if (files == null || files.length == 0) {
// 			throw new BusinessException(ReturnCode.RC400.getCode(), "上传文件不能为空");
// 		}
// 		if (allowedTypes == null || allowedTypes.isEmpty()) {
// 			throw new BusinessException(ReturnCode.RC400.getCode(), "允许的文件类型不能为空");
// 		}
//
// 		log.info("开始上传文件，数量: {}, 允许类型: {}", files.length, allowedTypes);
// 		long start = System.currentTimeMillis();
//
// 		List<Map<String, Object>> successFiles = new ArrayList<>();
// 		List<Map<String, Object>> errorFiles = new ArrayList<>();
//
// 		// 获取图片上传目录（相对路径）
// 		String uploadDir = fileUploadProperties.getImage().getUploadDir();
// 		// 获取安全的绝对路径
// 		Path uploadPath = PathUtil.getExternalPath(uploadDir);
//
// 		try {
// 			// 创建多级目录
// 			// 如果被创建文件夹的父文件夹不存在，就创建它
// 			// 如果被创建的文件夹已经存在，就是用已经存在的文件夹，不会重复创建，没有异常抛出
// 			// 如果因为磁盘IO出现异常，则抛出IOException
// 			Files.createDirectories(uploadPath);
// 		} catch (IOException e) {
// 			log.error("无法创建图片上传目录: {}", e.getMessage());
// 			throw new BusinessException(ReturnCode.RC500.getCode(), "系统内部错误: 无法创建上传目录");
// 		}
//
// 		for (MultipartFile file : files) {
// 			Map<String, Object> result = this.initResultMap(file);
// 			try {
// 				this.validateFile(file, allowedTypes);
// 				this.saveFile(file, uploadPath, result, successFiles);
// 			} catch (BusinessException e) {
// 				String errorMessage = e.getMessage();
// 				if (e.getCode() == ReturnCode.RC500.getCode()) errorMessage = "系统内部错误：" + errorMessage;
// 				handleError(result, errorMessage, errorFiles);
// 			} catch (Exception e) {
// 				handleError(result, "系统内部错误：" + e.getMessage(), errorFiles);
// 				log.error("文件上传异常", e);
// 			}
// 		}
//
// 		long end = System.currentTimeMillis();
// 		log.info("文件上传完成，耗时: {}ms, 成功: {}, 失败: {}", end - start, successFiles.size(), errorFiles.size());
//
// 		return Map.of(
// 				"total", files.length,
// 				"success", successFiles.size(),
// 				"failed", errorFiles.size(),
// 				"successFiles", successFiles,
// 				"errorFiles", errorFiles
// 		);
// 	}
//
//
// 	// 初始化上传结果
// 	private Map<String, Object> initResultMap(MultipartFile file) {
// 		Map<String, Object> result = new HashMap<>();
// 		result.put("originalName", file.getOriginalFilename());
// 		result.put("ok", false); // 默认文件上传失败
// 		return result;
// 	}
//
// 	// 上传文件校验
// 	private void validateFile(MultipartFile file, Set<MediaType> allowedTypes) {
// 		if (file.isEmpty()) {
// 			throw new BusinessException(ReturnCode.RC400.getCode(), "空文件");
// 		}
// 		if (!MediaTypeValidator.validate(file, allowedTypes)) {
// 			throw new BusinessException(ReturnCode.RC400.getCode(), "仅支持以下类型文件: " + allowedTypes);
// 		}
// 	}
//
// 	// 处理上传错误
// 	private void handleError(Map<String, Object> result, String message, List<Map<String, Object>> errorFiles) {
// 		result.put("error", message);
// 		errorFiles.add(result);
// 	}
//
// 	// 保存文件
// 	private void saveFile(MultipartFile file, Path uploadPath, Map<String, Object> result, List<Map<String, Object>> successFiles) throws IOException {
// 		// 生成唯一文件名
// 		String filename = generateSafeFilename(file);
// 		Path filePath = uploadPath.resolve(filename);
// 		file.transferTo(filePath);
// 		String accessUrl = fileUploadProperties.getImage().getAccessUrl();
// 		result.put("ok", true);
// 		result.put("url", accessUrl + filename);
// 		successFiles.add(result);
// 	}
//
//
// 	/*
// 	 * 生成唯一文件名
// 	 * @param originalName 原始文件名
// 	 * @return 唯一文件名
// 	 */
// 	private String generateSafeFilename(MultipartFile file) {
// 		String originalName = file.getOriginalFilename();
// 		String ext = "";
//
// 		int index = originalName.lastIndexOf('.');
// 		if (index > 0) {
// 			ext = originalName.substring(index);
// 			if (!ext.matches("\\.[a-zA-Z0-9]+")) {
// 				throw new BusinessException(ReturnCode.RC400.getCode(), "文件名不合法");
// 			}
// 		}
//
// 		return UUID.randomUUID().toString().replace("-", "") + ext;
// 	}
//
// 	// 类型校验工具类
// 	public static class MediaTypeValidator {
// 		// private static final Tika tika = new Tika();
// 		//
// 		// public static boolean validate(MultipartFile file, Set<MediaType> allowedTypes) {
// 		// 	try (InputStream is = file.getInputStream()) {
// 		// 		String detectedType = tika.detect(is);
// 		// 		MediaType actual = MediaType.parseMediaType(detectedType);
// 		//
// 		// 		return allowedTypes.stream().anyMatch(allowed -> {
// 		// 			if (allowed.isWildcardType()) {
// 		// 				return actual.getType().equals(allowed.getType());
// 		// 			}
// 		// 			return allowed.includes(actual);
// 		// 		});
// 		// 	} catch (IOException e) {
// 		// 		throw new BusinessException("文件类型检测失败", e);
// 		// 	}
// 		// }
//
// 		// 文件类型校验
// 		public static boolean validate(MultipartFile file, Set<MediaType> allowedTypes) {
// 			try {
// 				MediaType actual = MediaType.parseMediaType(file.getContentType());
// 				return allowedTypes.stream().anyMatch(allowed -> allowed.includes(actual));
// 			} catch (Exception e) {
// 				log.error("文件类型检测失败", e);
// 				throw new BusinessException(ReturnCode.RC500.getCode(), "文件类型检测失败");
// 			}
// 		}
// 	}
// }
//
