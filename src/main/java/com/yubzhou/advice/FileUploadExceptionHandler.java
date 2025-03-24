package com.yubzhou.advice;

import com.yubzhou.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Order(4)
@RestControllerAdvice
@Slf4j
public class FileUploadExceptionHandler {

	// @Value("${spring.servlet.multipart.max-file-size}")
	// private String maxFileSize;
	//
	// @Value("${spring.servlet.multipart.max-request-size}")
	// private String maxRequestSize;

	private final MultipartProperties multipartProperties;

	@Autowired
	public FileUploadExceptionHandler(MultipartProperties multipartProperties) {
		this.multipartProperties = multipartProperties;
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
	public Result<Void> handleMaxSizeException(MaxUploadSizeExceededException e) {
		log.error("MaxUploadSizeExceededException: {}", e.getMessage());
		String errorMessage = "单个文件大小超过限制，最大允许上传文件大小为：" + multipartProperties.getMaxFileSize().toMegabytes() +
				"MB，请压缩或减小文件大小后重新上传。";
		if (e.getMaxUploadSize() == multipartProperties.getMaxRequestSize().toBytes()) {
			errorMessage = "上传文件总大小超过限制，最大允许上传总大小为：" + multipartProperties.getMaxRequestSize().toMegabytes() + "MB，请减少文件数量或压缩文件后重新上传。";
		}
		return Result.fail(HttpStatus.PAYLOAD_TOO_LARGE.value(), errorMessage);
	}
}
