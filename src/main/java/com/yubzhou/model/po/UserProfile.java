package com.yubzhou.model.po;

import com.yubzhou.handler.StringToSetTypeHandler;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_profiles") // mybatis-plus注解，指定对应的数据库表名
public class UserProfile {
	@TableId // mybatis-plus注解，指定主键
	private Long id; // 主键

	private Long userId; // 用户id

	private String nickname; // 昵称

	private Short gender; // 性别（0:未知，1:男，2:女）

	private String avatarUrl; // 头像url

	@TableField(typeHandler = StringToSetTypeHandler.class) // mybatis-plus注解，指定自定义类型处理器
	private Set<String> interestedFields; // 兴趣领域
}
