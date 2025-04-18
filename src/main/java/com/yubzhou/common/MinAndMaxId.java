package com.yubzhou.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 存储数据库表中的最小ID和最大ID
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinAndMaxId {
	private Long minId;
	private Long maxId;
}