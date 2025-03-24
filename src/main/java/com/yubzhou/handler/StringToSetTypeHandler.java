package com.yubzhou.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class StringToSetTypeHandler extends BaseTypeHandler<Set<String>> {

	/*
	处理非空参数的写入
		作用：
			将 Java 对象（如 Set<String>）转换为 JDBC 类型（如 VARCHAR），用于 INSERT/UPDATE 等操作。
		参数说明：
			PreparedStatement ps：预编译的 SQL 语句对象。
			int i：参数位置（从 1 开始）。
			Set<String> parameter：待转换的 Java 对象（非空）。
			JdbcType jdbcType：JDBC 类型（可忽略）。
		调用时机：
			当执行 INSERT 或 UPDATE 操作，且实体类字段值非空 时触发。
	 */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Set<String> parameter, JdbcType jdbcType) throws SQLException {
		log.info("TypeHandler invoked for parameter: " + parameter); // 添加日志
		if (parameter == null || parameter.isEmpty()) {
			ps.setString(i, null);
			return;
		}
		// 将 Set 转换为逗号分隔的字符串
		String joined = String.join(",", parameter);
		ps.setString(i, joined);
	}


	// 处理结果集读取：三个重载方法，用于从不同数据源中读取结果

	/*
	按列名读取结果集，返回 Java 对象（如 Set<String>）
		作用：
			从 ResultSet 中通过列名（如 interested_fields）获取值，并转换为 Java 对象。
		参数说明：
			ResultSet rs：结果集对象。
			String columnName：列名。
		返回值：
			Java 对象（如 Set<String>）。
		调用场景：
			查询时明确使用列名获取数据（如 SELECT interested_fields FROM user_profile）。
	 */
	@Override
	public Set<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return parseStringToSet(value);
	}


	/*
	按列索引读取结果集，返回 Java 对象（如 Set<String>）
		作用：
			从 ResultSet 中通过列索引（从 1 开始）获取值，并转换为 Java 对象。
		参数说明：
			ResultSet rs：结果集对象。
			int columnIndex：列索引（从 1 开始）。
		返回值：
			Java 对象（如 Set<String>）。
		调用场景：
			查询时不确定列名，通过列索引获取数据（如 SELECT * FROM user_profile WHERE id = ?，按结果顺序处理）。
	 */
	@Override
	public Set<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		return parseStringToSet(value);
	}


	/*
	按列名读取存储过程调用结果集，返回 Java 对象（如 Set<String>）
		作用：
			从 CallableStatement（存储过程结果）中通过索引获取值，并转换为 Java 对象。
		参数说明：
			CallableStatement cs：存储过程调用结果集对象。
			int columnIndex：列索引（从 1 开始）。
		返回值：
			Java 对象（如 Set<String>）。
		调用场景：
			调用存储过程并处理输出参数时。
	 */
	@Override
	public Set<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		return parseStringToSet(value);
	}


	private Set<String> parseStringToSet(String value) {
		if (value == null || value.trim().isEmpty()) {
			return new HashSet<>();
		}
		// 分割字符串并过滤空值
		return Arrays.stream(value.split(","))
				.filter(s -> !s.trim().isEmpty())
				.collect(Collectors.toSet());
	}
}
