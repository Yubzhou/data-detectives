package com.yubzhou.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory,
													   @Qualifier("businessObjectMapper") ObjectMapper objectMapper) {
		// 创建键为String，值为Object的RedisTemplate
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		// 使用String序列化Key
		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringSerializer);
		template.setHashKeySerializer(stringSerializer);

		// 使用JSON序列化Value
		// 使用自定义ObjectMapper配置（使其可以支持Java8时间类型）
		Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
		template.setValueSerializer(jsonSerializer);
		template.setHashValueSerializer(jsonSerializer);

		template.afterPropertiesSet(); // 显式调用以确保属性设置完成后正确初始化模板
		return template;
	}

	// 配置Lua脚本
	@Bean("zSetRandomScript")
	public DefaultRedisScript<List> zSetRandomScript() {
		DefaultRedisScript<List> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("scripts/random_zset_exclude.lua"));
		script.setResultType(List.class);
		return script;
	}

	// 配置Lua脚本
	@Bean("deleteByPrefixScript")
	public DefaultRedisScript<Long> deleteByPrefixScript() {
		DefaultRedisScript<Long> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("scripts/delete_by_prefix.lua"));
		script.setResultType(Long.class);
		return script;
	}
}
