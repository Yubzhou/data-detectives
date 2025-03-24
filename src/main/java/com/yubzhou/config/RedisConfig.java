package com.yubzhou.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	private final ObjectMapper mapper;

	@Autowired
	public RedisConfig(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		// 创建键为String，值为Object的RedisTemplate
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		// mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		// mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
		// 		JsonTypeInfo.As.PROPERTY);

		// 使用Jackson2JsonRedisSerializer序列化和反序列化redis的value值
		// 使用全局ObjectMapper配置（使其可以支持Java8时间类型）
		Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(mapper,
				Object.class);

		// 设置key和hashKey的序列化器
		template.setKeySerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
		// 设置value和hashValue的序列化器
		template.setValueSerializer(jsonRedisSerializer);
		template.setHashValueSerializer(jsonRedisSerializer);
		template.afterPropertiesSet(); // 显式调用以确保属性设置完成后正确初始化模板
		return template;
	}

	// @Bean
	// public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
	// 	// 创建键为String，值为Object的RedisTemplate
	// 	RedisTemplate<String, Object> template = new RedisTemplate<>();
	// 	template.setConnectionFactory(factory);
	//
	// 	mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
	// 	mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
	// 			JsonTypeInfo.As.PROPERTY);
	//
	// 	GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(mapper);
	//
	// 	// 设置key和hashKey的序列化器
	// 	template.setKeySerializer(RedisSerializer.string());
	// 	template.setHashKeySerializer(RedisSerializer.string());
	// 	// 设置value和hashValue的序列化器
	// 	template.setValueSerializer(jsonRedisSerializer);
	// 	template.setHashValueSerializer(jsonRedisSerializer);
	// 	template.afterPropertiesSet(); // 显式调用以确保属性设置完成后正确初始化模板
	// 	return template;
	// }
}
