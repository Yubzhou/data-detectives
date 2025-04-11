package com.yubzhou.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {
	// 角色定义（可根据业务需求扩展）
	SUPER_ADMIN("SUPER_ADMIN", "超级管理员", 0, Arrays.asList("DELETE", "MANAGE_ADMIN")),
	ADMIN("ADMIN", "系统管理员", 1, Arrays.asList("MANAGE_USER", "EDIT_CONTENT")),
	USER("USER", "注册用户", 2, Collections.singletonList("VIEW_CONTENT"));
	// GUEST("GUEST", "访客用户", 3, Collections.singletonList("VIEW_PUBLIC"));

	private final String code;          // 角色编码（唯一标识）
	private final String displayName;   // 显示名称
	@EnumValue
	// mybatis-plus库注解，指定存储到数据库的枚举值为 hierarchyLevel 值（使用该注解后，mybatis-plus会自动将枚举值映射为数据库中的整数值，且自动将数据库中的整数值映射为枚举值）
	private final int hierarchyLevel;   // 权限层级（数值越小权限越高）
	/**
	 * -- GETTER --
	 * 获取所有权限列表（不可修改）
	 */
	private final List<String> permissions; // 关联权限列表

	UserRole(String code, String displayName, int hierarchyLevel, List<String> permissions) {
		this.code = code;
		this.displayName = displayName;
		this.hierarchyLevel = hierarchyLevel;
		this.permissions = Collections.unmodifiableList(permissions);
	}

	/**
	 * 判断当前角色是否包含指定权限
	 */
	public boolean hasPermission(String permission) {
		return this.permissions.contains(permission);
	}

	/**
	 * 判断当前角色是否拥有高于或等于目标角色的权限层级
	 */
	public boolean isHigherOrEqual(UserRole targetRole) {
		return this.hierarchyLevel <= targetRole.hierarchyLevel;
	}

	public boolean isAdmin() {
		return isHigherOrEqual(ADMIN);
	}

	public boolean isSuperAdmin() {
		return this.isHigherOrEqual(SUPER_ADMIN);
	}

	/**
	 * 根据角色编码查找枚举（大小写敏感）
	 */
	public static UserRole fromCode(String code) {
		for (UserRole role : values()) {
			if (role.code.equals(code)) {
				return role;
			}
		}
		throw new IllegalArgumentException("无效的角色编码: " + code);
	}

	/**
	 * 获取所有角色编码列表
	 */
	public static List<String> getAllRoleCodes() {
		return Arrays.stream(values())
				.map(UserRole::getCode)
				.collect(Collectors.toList());
	}

	// /**
	//  * 使用示例
	//  */
	// public static void main(String[] args) {
	// 	// 角色权限检查
	// 	UserRole editor = UserRole.EDITOR;
	// 	System.out.println(editor.hasPermission("EDIT_CONTENT")); // true
	// 	System.out.println(editor.hasPermission("DELETE"));       // false
	//
	// 	// 层级比较
	// 	System.out.println(editor.isHigherOrEqual(UserRole.USER)); // true
	// 	System.out.println(editor.isHigherOrEqual(UserRole.ADMIN));// false
	//
	// 	// 根据编码获取角色
	// 	UserRole admin = UserRole.fromCode("ADMIN");
	// 	System.out.println(admin.getDisplayName()); // 输出：系统管理员
	//
	// 	// 获取所有角色编码
	// 	System.out.println(UserRole.getAllRoleCodes());
	// 	// 输出：[SUPER_ADMIN, ADMIN, EDITOR, USER, GUEST]
	// }
}