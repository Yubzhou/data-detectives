package com.yubzhou.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页响应结果封装
 *
 * @param <T> 数据类型
 */
@Getter
@Setter
public class PageResult<T> {
	// 核心分页字段
	private List<T> records;      // 当前页数据列表
	private long currentPage;     // 当前页码
	private long pageSize;        // 每页数量
	private long totalRecords;    // 总记录数
	private long totalPages;      // 总页数

	// 扩展分页状态字段（根据需求可选）
	private boolean hasPrevious; // 是否有上一页
	private boolean hasNext;     // 是否有下一页

	/**
	 * 完整构造方法
	 */
	public PageResult(List<T> records, long currentPage, long pageSize,
					  long totalRecords, long totalPages) {
		this.records = records;
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.totalRecords = totalRecords;
		this.totalPages = totalPages;
		this.hasPrevious = currentPage > 1L;
		this.hasNext = currentPage < totalPages;
	}

	/**
	 * 从MyBatis-Plus Page对象构造
	 */
	public PageResult(IPage<T> page) {
		this(page.getRecords(),
				page.getCurrent(),
				page.getSize(),
				page.getTotal(),
				page.getPages());
	}

	/**
	 * 通过 MyBatis-Plus 的 IPage 对象快速构建
	 */
	public static <T> PageResult<T> of(IPage<T> page) {
		return new PageResult<>(page);
	}

	/**
	 * 空页响应快捷方法
	 */
	public static <T> PageResult<T> empty() {
		return new PageResult<>(
				Collections.emptyList(), // 返回空列表，避免NPE（空指针异常）
				1,
				0,
				0,
				0
		);
	}

	// 空页判断
	@JsonProperty("isEmpty")
	public boolean isEmpty() {
		return records == null || records.isEmpty();
	}

	// 分页参数校验
	@JsonProperty("isValidPage")
	public boolean isValidPage() {
		return currentPage > 0
				&& pageSize > 0
				&& currentPage <= totalPages;
	}
}