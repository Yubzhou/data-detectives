package com.yubzhou.convert;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
		// 标明当前使用容器为spring，生成的实现类会带有@Component注解，可以通过Spring的@Autowired注入（生成在target/generated-sources/annotations/目录下）
		componentModel = "spring",
		// 当来源类中没有对应属性时的策略
		unmappedSourcePolicy = ReportingPolicy.IGNORE,
		// 当目标类中没有对应属性时的策略
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		// 当源对象为null时的策略
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
		// 当源对象的某个属性值为 null 时应对的策略
		nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.SET_TO_NULL,
		// 有损转换的处理策略，例如：long 转换为 int
		typeConversionPolicy = ReportingPolicy.WARN
)
public interface GlobalConvertConfig {
}