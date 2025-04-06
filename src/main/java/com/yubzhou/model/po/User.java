package com.yubzhou.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubzhou.common.UserRole;
import com.yubzhou.common.UserStatus;
import com.yubzhou.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users") // mybatis-plus注解，指定对应的数据库表名
public class User {
	@TableId // 标记为主键
	private Long id; // 用户ID

	@JsonIgnore // 手机号不返回给前端
	private String phone; // 手机号

	@JsonIgnore // 密码不返回给前端
	private String password; // 密码

	private UserRole role; // 用户角色

	private UserStatus status; // 用户状态

	@JsonFormat(pattern = DateTimeUtil.LOCAL_DATE_TIME_NO_MILLIS_FORMAT)
	private LocalDateTime createdAt; // 创建时间

	@JsonFormat(pattern = DateTimeUtil.LOCAL_DATE_TIME_NO_MILLIS_FORMAT)
	private LocalDateTime updatedAt; // 更新时间

	@JsonFormat(pattern = DateTimeUtil.LOCAL_DATE_TIME_NO_MILLIS_FORMAT)
	private LocalDateTime lastLoginAt; // 最后登录时间
}
