package com.yubzhou.util;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
public class PathUtil {

	private static final String PROJECT_PATH = System.getProperty("user.dir");

	/**
	 * 获取相当于项目根目录的绝对路径
	 * 比如：
	 * ./uploads 表示与项目同级的uploads目录
	 * ../common 表示与项目目录的上一级目录同级的common目录
	 *
	 * @param relativePath 相对路径（如./uploads，与项目同级的uploads目录）
	 */
	public static Path getExternalPath(String relativePath) {
		Path projectPath = Paths.get(PROJECT_PATH);
		// log.info("project path: {}", projectPath);
		return projectPath.resolveSibling(relativePath).normalize();
	}


	/*
	 * 删除文件或目录（包括目录及其内容）
	 *
	 * @param path 要删除的文件或目录路径
	 * @throws IOException 如果文件或目录不存在或无法访问
	 */
	public static void deleteFileOrDirectory(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new NoSuchFileException("路径不存在: " + path);
		}

		if (Files.isDirectory(path)) {
			// 删除目录及其内容
			Files.walkFileTree(path, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file); // 删除文件
					log.info("已删除文件: {}", file);
					return FileVisitResult.CONTINUE; // 继续遍历下一个文件
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc != null) {
						throw exc; // 确保异常传递
					}
					Files.delete(dir); // 删除已清空的目录
					log.info("已删除目录: {}", dir);
					return FileVisitResult.CONTINUE; // 继续遍历下一个目录
				}
			});

			log.info("已删除目录及其内容: {}", path);
		} else {
			// 删除单个文件
			Files.delete(path);
			log.info("已删除文件: {}", path);
		}
	}


	/**
	 * 清空目录内容但保留目录结构
	 *
	 * @param directory 要清空的目录路径
	 * @throws IOException 如果目录不存在或无法访问
	 */
	public static void emptyDirectory(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			throw new NoSuchFileException("目录不存在: " + directory);
		}
		if (!Files.isDirectory(directory)) {
			throw new IllegalArgumentException("路径不是目录: " + directory);
		}

		Files.walkFileTree(directory, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				log.info("已删除文件: {}", file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc != null) {
					throw exc;
				}
				// 不删除根目录
				if (!dir.equals(directory)) {
					Files.delete(dir);
					log.info("已删除子目录: {}", dir);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		log.info("目录已清空（保留结构）: {}", directory);
	}

	// 移动文件
	public static void moveFile(Path source, Path targetDir) throws IOException {
		// 校验源路径是否存在且是文件（如果路径不存在则直接返回false）
		if (!Files.isRegularFile(source)) {
			log.warn("源路径不是文件，取消移动操作: {}", source);
			throw new IllegalArgumentException("源路径不是文件：" + source);
		}
		try {
			// 确保目标目录存在（不存在则创建，存在则不会抛出异常）
			Files.createDirectories(targetDir);

			// 构建目标路径（保留原文件名）
			Path target = targetDir.resolve(source.getFileName());

			// 执行移动操作
			Files.move(
					source,
					target,
					StandardCopyOption.REPLACE_EXISTING, // 覆盖已存在的目标文件或空目录
					// StandardCopyOption.COPY_ATTRIBUTES, // 保留源文件的元数据
					StandardCopyOption.ATOMIC_MOVE // // 确保移动操作的原子性（全有或全无）
			);

			log.info("移动文件成功: {} -> {}", source, target);
		} catch (Exception e) {
			log.error("移动文件失败: ", e);
		}
	}

	public static void moveDirectoryAsSubDir(Path sourceDir, Path targetParentDir) throws IOException {
		// 校验源路径是否存在且是目录
		if (!Files.isDirectory(sourceDir)) {
			log.warn("源路径不是目录，取消移动操作: {}", sourceDir);
			throw new IllegalArgumentException("源路径不是目录: " + sourceDir);
		}

		// 计算目标路径（targetParentDir/targetDirName）
		// getFileName方法会获取到路径的最后一层，无论其为目录或文件（如果为目录就获取到最后一层目录名，为文件则获取文件名）
		Path targetDirName = sourceDir.getFileName();
		if (targetDirName == null) {
			log.warn("源路径没有目录名，取消移动操作: {}", sourceDir);
			throw new IllegalArgumentException("源路径没有目录名: " + sourceDir);
		}
		Path targetDir = targetParentDir.resolve(targetDirName);

		// 确保目标父目录（targetParentDir）存在
		Files.createDirectories(targetParentDir);

		// 检查目标路径（targetParentDir/targetDirName）状态
		if (Files.exists(targetDir)) {
			if (!Files.isDirectory(targetDir)) {
				throw new IOException("目标路径已存在但不是目录: " + targetDir);
			}
			if (isDirectoryNotEmpty(targetDir)) { // 检查是否非空
				throw new IOException("目标目录已存在且非空: " + targetDir);
			}
		}

		// 执行移动操作（覆盖空目录）
		try {
			Files.move(
					sourceDir,
					targetDir,
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.ATOMIC_MOVE
			);

			log.info("移动目录成功: {} -> {}", sourceDir, targetDir);
		} catch (Exception e) {
			log.error("移动目录失败: ", e);
		}
	}

	// 检查目录是否存在且非空
	private static boolean isDirectoryNotEmpty(Path dir) throws IOException {
		// 如果不存在或不是目录，则返回false
		if (!Files.isDirectory(dir)) {
			return false;
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			return stream.iterator().hasNext();
		}
	}


	/**
	 * 删除多个文件
	 *
	 * @param paths 要删除的文件路径列表
	 */
	public static void deleteFiles(@NonNull Collection<Path> paths) {
		if (paths.isEmpty()) {
			log.warn("paths集合为空，取消删除操作");
			return;
		}

		for (Path path : paths) {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				log.error("删除文件失败: {}", path, e);
			}
		}
	}

	/**
	 * 删除多个文件
	 *
	 * @param baseDir   文件基础目录（即要删除文件集合的父目录）
	 * @param fileNames 要删除的文件名列表
	 */
	public static void deleteFilesWithFilePath(@NonNull Path baseDir, @NonNull Collection<String> fileNames) {
		// 参数校验
		if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
			throw new IllegalArgumentException("baseDir必须是一个已存在的目录");
		}
		if (fileNames.isEmpty()) {
			log.warn("fileNames集合为空，取消删除操作");
			return;
		}

		Path toDeletePath;
		for (String fileName : fileNames) {
			toDeletePath = baseDir.resolve(fileName).normalize();
			try {
				Files.deleteIfExists(toDeletePath);
			} catch (IOException e) {
				log.error("删除文件失败: {}", toDeletePath, e);
			}
		}
	}

	/**
	 * 删除指定目录下的未在文件名集合的文件
	 *
	 * @param baseDir 文件基础目录（即要删除文件集合的父目录）
	 * @param toKeep  要保留的文件名集合（即未在该集合里面的文件都删除）
	 */
	public static Map<String, Integer> deleteFilesWithFileName(@NonNull Path baseDir, @NonNull Set<String> toKeep) {
		// 参数校验
		if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
			throw new IllegalArgumentException("baseDir必须是一个已存在的目录");
		}

		AtomicInteger total = new AtomicInteger();
		AtomicInteger success = new AtomicInteger();
		AtomicInteger fail = new AtomicInteger();
		try (Stream<Path> fileStream = Files.list(baseDir)) {
			fileStream.forEach(path -> {
				// 只处理普通文件，忽略目录和符号链接
				if (!Files.isRegularFile(path)) return;
				Path currentFileName = path.getFileName();
				if (currentFileName != null && !toKeep.contains(currentFileName.toString())) {
					try {
						Files.deleteIfExists(path); // 删除文件
						success.getAndIncrement();
						log.info("已删除文件: {}", path);
					} catch (IOException e) {
						fail.getAndIncrement();
						log.error("删除文件失败: {}", path, e);
					}
				}
				total.getAndIncrement();
			});
		} catch (IOException e) {
			log.error("无法访问目录: {}", baseDir, e);
		}
		// 输出统计信息：总计，已删除文件数量，删除失败文件个数，剩余文件数量
		// log.info("删除文件统计：总计={}, 已删除={}, 删除失败={}, 剩余={}", total.get(), success.get(), fail.get(), total.get() - success.get());

		// 返回统计数据
		return Map.of(
				"total", total.get(),
				"success", success.get(),
				"fail", fail.get(),
				"remain", total.get() - success.get()
		);
	}


	/**
	 * 并行删除多个文件
	 *
	 * @param paths 要删除的文件路径列表
	 */
	public static void deleteFilesParallel(@NonNull Collection<Path> paths) {
		if (paths.isEmpty()) {
			log.warn("paths集合为空，取消删除操作");
			return;
		}

		paths.parallelStream().forEach(path -> {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				log.error("删除文件失败: {}", path, e);
			}
		});
	}

	/**
	 * 并行删除指定目录下的未在文件名集合的文件
	 *
	 * @param baseDir   文件基础目录（即要删除文件集合的父目录）
	 * @param fileNames 要删除的文件名集合
	 */
	public static void deleteFilesParallel(@NonNull Path baseDir, @NonNull Collection<String> fileNames) {
		// 参数校验
		if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
			throw new IllegalArgumentException("baseDir必须是一个已存在的目录");
		}
		if (fileNames.isEmpty()) {
			log.warn("fileNames集合为空，取消删除操作");
			return;
		}

		List<Path> paths = fileNames.stream()
				.map(fileName -> baseDir.resolve(fileName).normalize()).toList();
		deleteFilesParallel(paths);
	}


	public static void main(String[] args) {
		AtomicInteger count = new AtomicInteger();
		System.out.println(count.getAndIncrement());
		System.out.println(count);

		// Path path = Paths.get("./uploads/images/2023/09/01/test.txt");
		// log.info("path: {}", path);
		// log.info("path filename: {}", path.getFileName());
		// log.info("path exists: {}", Files.exists(path));
		// log.info("path isRegularFile: {}", Files.isRegularFile(path));
		// log.info("path isDirectory: {}", Files.isDirectory(path));

		// // 删除文件或目录
		// Path targetDir = Paths.get("D:/desktop/temp/c");
		// try {
		// 	long startTime = System.currentTimeMillis();
		// 	deleteFileOrDirectory(targetDir);
		// 	long endTime = System.currentTimeMillis();
		// 	log.info("删除文件耗时: {}ms", endTime - startTime);
		// } catch (IOException e) {
		// 	log.error("删除文件失败: ", e);
		// }

		// // 清空目录内容但保留目录结构
		// Path targetDir = Paths.get("D:/desktop/temp/b");
		// try {
		// 	long startTime = System.currentTimeMillis();
		// 	emptyDirectory(targetDir);
		// 	long endTime = System.currentTimeMillis();
		// 	log.info("清空目录耗时: {}ms", endTime - startTime);
		// } catch (IOException e) {
		// 	log.error("清空目录失败: ", e);
		// }


		// // 移动文件操作
		// Path source = Paths.get("D:/desktop/temp/b/test.txt");
		// Path targetDir = Paths.get("D:/desktop/temp/c");
		// try {
		// 	long startTime = System.currentTimeMillis();
		// 	moveFile(source, targetDir);
		// 	long endTime = System.currentTimeMillis();
		// 	log.info("移动文件耗时: {}ms", endTime - startTime);
		// } catch (IOException e) {
		// 	log.error("移动文件失败: ", e);
		// }

		// // 移动目录操作
		// Path sourceDir = Paths.get("D:/desktop/temp/b/b1");
		// Path targetParent = Paths.get("D:/desktop/temp/c");
		// try {
		// 	long startTime = System.currentTimeMillis();
		// 	moveDirectoryAsSubDir(sourceDir, targetParent);
		// 	long endTime = System.currentTimeMillis();
		// 	log.info("移动目录耗时: {}ms", endTime - startTime);
		// } catch (IOException e) {
		// 	log.error("移动目录失败: ", e);
		// }
	}
}