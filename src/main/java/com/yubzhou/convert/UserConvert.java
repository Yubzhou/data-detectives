package com.yubzhou.convert;

import com.yubzhou.model.dto.UserDto;
import com.yubzhou.model.po.User;
import com.yubzhou.common.UserRole;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(
		config = GlobalConvertConfig.class, // 使用全局配置
		imports = {LocalDateTime.class, UserRole.class} // 声明需要使用的类，可以在表达式(expression)中直接使用
		// uses = {UserMapper.class} // 声明需要使用的Mapper类，会在生成的实现类中自动注入依赖（前提是componentModel="spring"）
)
public interface UserConvert {

	// 实例获取方式（当componentModel="spring"时推荐使用依赖注入）
	// UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	/**
	 * 将User实体转换为UserDto
	 *
	 * @param user 源实体
	 * @return 目标DTO
	 */
	// 仅需处理需要特殊控制的字段
	@Mapping(target = "password", ignore = true)
	// 忽略密码字段（因为DTO不需要返回密码）
	UserDto toDto(User user);

	/**
	 * 将UserDto转换为User实体
	 *
	 * @param userDto 源DTO
	 * @return 目标实体
	 */
	// target：指定目标字段名
	// ignore = true：忽略该字段的映射
	// defaultValue：当源字段为null时的默认值
	// expression：使用Java表达式进行赋值
	@Mapping(target = "id", ignore = true)
	// 忽略ID（由数据库生成）
	User toPo(UserDto userDto);

	/**
	 * 更新现有实体（可选）
	 *
	 * @param userDto 源DTO
	 * @param user    目标实体（会被修改）
	 */
	// @BeanMapping注解用于指定映射规则，nullValuePropertyMappingStrategy用于指定null值属性的映射策略
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateFromDto(UserDto userDto, @MappingTarget User user);
}