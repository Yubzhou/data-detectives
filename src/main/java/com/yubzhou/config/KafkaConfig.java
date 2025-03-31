package com.yubzhou.config;

import com.yubzhou.common.KafkaConstant;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

	@Bean
	public NewTopic userActionsTopic() {
		// 创建一个名为 user_actions 的 topic，分区数为 3，副本数为 1
		return new NewTopic(KafkaConstant.USER_ACTION_TOPIC, KafkaConstant.USER_ACTION_PARTITIONS, KafkaConstant.USER_ACTION_REPLICATION_FACTOR);
	}

}
