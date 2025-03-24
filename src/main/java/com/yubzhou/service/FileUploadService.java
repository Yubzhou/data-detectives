package com.yubzhou.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface FileUploadService {
	CompletableFuture<UploadResult> uploadImage(MultipartFile[] files, Set<MediaType> allowedTypes);

	CompletableFuture<UploadResult> uploadImages(MultipartFile[] files, Set<MediaType> allowedTypes);

	// 定义统一返回结构
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	class UploadResult {
		private int total; // 总文件数
		private int success; // 成功文件数
		private int failed; // 失败文件数
		private Queue<SuccessInfo> successFiles; // 成功文件信息
		private Queue<ErrorInfo> errorFiles; // 失败文件信息

		// 创建一个SuccessInfo对象
		public static SuccessInfo createSuccessInfo(String originalName, String url) {
			return new SuccessInfo(originalName, url);
		}

		// 创建一个ErrorInfo对象
		public static ErrorInfo createErrorInfo(String originalName, String error) {
			return new ErrorInfo(originalName, error);
		}

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		// 文件上传成功信息
		public static class SuccessInfo {
			private String originalName; // 原始文件名
			private String url; // 文件访问地址
		}

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		// 文件上传失败信息
		public static class ErrorInfo {
			private String originalName; // 原始文件名
			private String error; // 错误信息
		}
	}

	class FileTypeDetector {
		private static final Detector DETECTOR = new AutoDetectParser().getDetector();

		public static org.apache.tika.mime.MediaType detectRealType(MultipartFile file) throws IOException {
			byte[] headerBytes = new byte[1024];
			try (InputStream stream = file.getInputStream()) {
				stream.read(headerBytes); // 仅读取前 1KB
			}
			return DETECTOR.detect(
					TikaInputStream.get(headerBytes),
					new Metadata()
			);
		}
	}

	class MediaTypeDescriptions {

		private static final Map<MediaType, Description> DESCRIPTIONS = new HashMap<>();

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		public static class Description {
			private String description;
			private Set<String> extensions;

			public static Description of(String description, Set<String> extensions) {
				return new Description(description, extensions);
			}

			// 判断是否包含指定扩展名
			public boolean hasExtension(String extension) {
				// 当扩展名为空时，表示允许所有扩展名
				return extensions.isEmpty() || extensions.contains(extension);
			}
		}

		static {
			// 初始化一些常见的媒体类型描述
			DESCRIPTIONS.put(new MediaType("image", "*"), Description.of("图片文件", Collections.emptySet()));
			DESCRIPTIONS.put(new MediaType("image", "jpeg"), Description.of("JPEG图片", Set.of("jpg", "jpeg")));
			DESCRIPTIONS.put(new MediaType("image", "png"), Description.of("PNG图片", Set.of("png")));
			DESCRIPTIONS.put(new MediaType("image", "gif"), Description.of("GIF图片", Set.of("gif")));
			DESCRIPTIONS.put(new MediaType("image", "webp"), Description.of("WebP图片", Set.of("webp")));
			DESCRIPTIONS.put(new MediaType("image", "avif"), Description.of("AVIF图片", Set.of("avif")));
			DESCRIPTIONS.put(new MediaType("image", "svg+xml"), Description.of("SVG图片", Set.of("svg")));
			DESCRIPTIONS.put(new MediaType("image", "vnd.microsoft.icon"), Description.of("ICO图标", Set.of("ico")));

			DESCRIPTIONS.put(new MediaType("text", "*"), Description.of("文本文件", Collections.emptySet()));
			DESCRIPTIONS.put(new MediaType("text", "html"), Description.of("HTML文本文件", Set.of("html", "htm")));
			DESCRIPTIONS.put(new MediaType("text", "css"), Description.of("CSS文件", Set.of("css")));
			DESCRIPTIONS.put(new MediaType("text", "javascript"), Description.of("JavaScript文件", Set.of("js")));
			DESCRIPTIONS.put(new MediaType("text", "csv"), Description.of("CSV文件", Set.of("csv")));
			DESCRIPTIONS.put(new MediaType("text", "plain"), Description.of("TXT纯文本文件", Set.of("txt")));
			DESCRIPTIONS.put(new MediaType("text", "markdown"), Description.of("Markdown文件", Set.of("md", "markdown")));
			DESCRIPTIONS.put(new MediaType("text", "xml"), Description.of("XML文件", Set.of("xml")));

			DESCRIPTIONS.put(new MediaType("application", "javascript"), Description.of("JavaScript文件", Set.of("js")));
			DESCRIPTIONS.put(new MediaType("application", "pdf"), Description.of("PDF文件", Set.of("pdf")));
			DESCRIPTIONS.put(new MediaType("application", "json"), Description.of("JSON文件", Set.of("json")));
			DESCRIPTIONS.put(new MediaType("application", "xml"), Description.of("XML文件", Set.of("xml")));
			DESCRIPTIONS.put(new MediaType("application", "yaml"), Description.of("YAML文件", Set.of("yaml", "yml")));
			DESCRIPTIONS.put(new MediaType("application", "zip"), Description.of("ZIP压缩文件", Set.of("zip")));
			DESCRIPTIONS.put(new MediaType("application", "x-7z-compressed"), Description.of("7z压缩文件", Set.of("7z")));

			DESCRIPTIONS.put(new MediaType("video", "*"), Description.of("视频文件", Collections.emptySet()));
			DESCRIPTIONS.put(new MediaType("video", "mp4"), Description.of("MP4视频文件", Set.of("mp4")));
			DESCRIPTIONS.put(new MediaType("video", "mpeg"), Description.of("MPEG视频文件", Set.of("mpeg")));
			DESCRIPTIONS.put(new MediaType("video", "x-msvideo"), Description.of("AVI视频文件", Set.of("avi")));

			DESCRIPTIONS.put(new MediaType("audio", "*"), Description.of("音频文件", Collections.emptySet()));
			DESCRIPTIONS.put(new MediaType("audio", "mpeg"), Description.of("MP3音频文件", Set.of("mp3")));
			DESCRIPTIONS.put(new MediaType("audio", "aac"), Description.of("AAC音频文件", Set.of("aac")));
		}

		/**
		 * 获取媒体类型的中文描述
		 *
		 * @param mediaType 媒体类型
		 * @return 中文描述，如果不存在则返回默认值
		 */
		public static Description getDescription(MediaType mediaType) {
			// 如果不存在，则返回默认值
			return DESCRIPTIONS.getOrDefault(mediaType, Description.of(mediaType.toString(), Collections.emptySet()));
		}

		public static String getDescriptionName(MediaType mediaType) {
			return getDescription(mediaType).getDescription();
		}

		public static List<Description> getDescriptions(Set<MediaType> mediaTypes) {
			return mediaTypes.stream().map(MediaTypeDescriptions::getDescription).toList();
		}

		public static List<String> getDescriptionsName(Set<MediaType> mediaTypes) {
			return mediaTypes.stream().map(MediaTypeDescriptions::getDescriptionName).toList();
		}

		/**
		 * 获取媒体类型的扩展名列表
		 *
		 * @param mediaType 媒体类型
		 * @return 扩展名列表，如果不存在则返回空列表
		 */
		public static Set<String> getExtensions(MediaType mediaType) {
			return getDescription(mediaType).getExtensions();
		}

		public static boolean isExtensionValid(MediaType mediaType, String extension) {
			return getDescription(mediaType).hasExtension(extension);
		}
	}
}
