package com.yubzhou.producer;

import com.yubzhou.common.KafkaConstant;
import com.yubzhou.common.UserActionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NewsActionProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	public NewsActionProducer(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	// 处理用户行为事件（比如浏览、收藏、评论新闻等）
	public void sendActionEvent(Long newsId, Long userId, UserActionEvent.ActionType action) {
		UserActionEvent event = new UserActionEvent(
				newsId,
				userId,
				action,
				System.currentTimeMillis()
		);

		// Kafka分区策略：按新闻ID分区保证顺序
		int partition = newsId.hashCode() % KafkaConstant.USER_ACTION_PARTITIONS;
		kafkaTemplate.send(
				KafkaConstant.USER_ACTION_TOPIC, // 主题
				partition, // 分区
				String.valueOf(newsId), // key
				event // value
		);
	}
}
