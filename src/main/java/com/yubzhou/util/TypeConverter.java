package com.yubzhou.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TypeConverter {
	// 类型转换器注册表（可扩展）
	private static final Map<Class<?>, Function<String, ?>> CONVERTERS = new HashMap<>();

	static {
		// 注册默认类型转换器
		registerDefaultConverters();
	}

	private static void registerDefaultConverters() {
		CONVERTERS.put(Long.class, Long::parseLong);
		CONVERTERS.put(Integer.class, Integer::parseInt);
		CONVERTERS.put(Double.class, Double::parseDouble);
		CONVERTERS.put(String.class, s -> s);
		CONVERTERS.put(Boolean.class, Boolean::parseBoolean);
		// 添加更多基础类型...
	}

	/**
	 * 通用集合类型转换方法
	 *
	 * @param source            源集合
	 * @param targetType        目标元素类型
	 * @param collectionFactory 目标集合工厂（比如：ArrayList::new）
	 * @param <T>               目标元素类型
	 * @param <C>               目标集合类型
	 * @return 转换后的新集合
	 */
	public static <T, C extends Collection<T>> C convert(Collection<?> source,
														 Class<T> targetType,
														 Supplier<C> collectionFactory) {
		// 获取类型转换器
		Function<String, T> converter = getConverter(targetType);

		return source.stream()
				.map(obj -> safeConvert(obj, converter))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(collectionFactory));
	}

	/**
	 * 注册自定义类型转换器
	 *
	 * @param targetType 目标类型
	 * @param converter  转换函数
	 * @param <T>        目标类型泛型
	 */
	public static <T> void registerConverter(Class<T> targetType, Function<String, T> converter) {
		CONVERTERS.put(targetType, converter);
	}

	/**
	 * 安全转换（处理空值和异常）
	 */
	@SuppressWarnings("unchecked")
	private static <T> Function<String, T> getConverter(Class<T> targetType) {
		Function<String, ?> converter = CONVERTERS.get(targetType);
		if (converter == null) {
			throw new IllegalArgumentException("No converter for type: " + targetType);
		}
		return (Function<String, T>) converter;
	}

	private static <T> T safeConvert(Object obj, Function<String, T> converter) {
		try {
			return obj != null ? converter.apply(obj.toString()) : null;
		} catch (Exception e) {
			return null; // 转换失败返回null，后续会被过滤
		}
	}
}