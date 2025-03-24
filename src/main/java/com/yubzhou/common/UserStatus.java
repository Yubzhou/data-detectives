package com.yubzhou.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
	// 禁用、启用、锁定、注销
	DISABLED(0, "禁用"),
	NORMAL(1, "正常"),
	LOCKED(2, "锁定"),
	LOGOUT(3, "注销");

	@EnumValue // 使用该注解后，mybatis-plus会自动将枚举值映射为数据库中的整数值，且自动将数据库中的整数值映射为枚举值
	private final int code;          // 状态码（唯一标识）
	private final String displayName;   // 显示名称
}
