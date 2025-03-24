package com.yubzhou.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {

	private static final String PROJECT_PATH = System.getProperty("user.dir");

	/**
	 * 获取相当于项目根目录的绝对路径
	 * 比如：
	 *     ./uploads 表示与项目同级的uploads目录
	 *      ../common 表示与项目目录的上一级目录同级的common目录
	 *
	 * @param relativePath 相对路径（如./uploads，与项目同级的uploads目录）
	 */
	public static Path getExternalPath(String relativePath) {
		Path projectPath = Paths.get(PROJECT_PATH);
		return projectPath.resolveSibling(relativePath).normalize();
	}
}