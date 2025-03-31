// package com.yubzhou.consumer;
//
// import com.yubzhou.model.po.News;
// import com.yubzhou.util.HotNewsUtil;
// import com.yubzhou.common.KafkaConstant;
// import com.yubzhou.common.RedisConstant;
// import com.yubzhou.common.UserActionEvent;
// import com.yubzhou.common.UserActionEvent.ActionType;
// import com.yubzhou.util.RedisUtil;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;
//
// import java.util.Map;
// import java.util.concurrent.TimeUnit;
//
// @Service
// @Slf4j
// public class HotNewsService {
// 	private final RedisUtil redisUtil;
//
// 	@Autowired
// 	public HotNewsService(RedisUtil redisUtil) {
// 		this.redisUtil = redisUtil;
// 	}
//
// 	// 监听用户行为事件
// 	@KafkaListener(topics = KafkaConstant.USER_ACTION_TOPIC, groupId = KafkaConstant.USER_ACTION_GROUP_ID)
// 	public void processEvent(UserActionEvent event) {
// 		log.info("Received user action event: {}", event);
// 		// 更新新闻指标
// 		updateMetricsAndHotness(event);
// 		// 更新用户新闻行为记录
// 		updateUserNewsAction(event);
// 	}
//
// 	public News getNewsMetrics(Long newsId) {
// 		String metricsKey = RedisConstant.NEWS_METRICS_PREFIX + newsId;
// 		Map<Object, Object> data = redisUtil.hmget(metricsKey);
//
// 		if (data.isEmpty()) {
// 			// 从数据库加载并设置空值保护
// 			Map<String, Integer> dbData = loadFromMySQL(newsId);
// 			if (!dbData.isEmpty()) {
// 				redisUtil.hmset(metricsKey, dbData);
// 				redisUtil.expire(metricsKey, 300 + new Random().nextInt(600));
// 			} else {
// 				redisUtil.hset(metricsKey, "empty", "1");
// 				redisUtil.expire(metricsKey, 60);
// 			}
// 			return dbData;
// 		}
//
// 		return convertMap(data);
// 	}
//
// 	// 更新新闻指标并刷新1小时热度
// 	public void updateMetricsAndHotness(UserActionEvent event) {
// 		// 获取新闻指标Key
// 		String metricsKey = RedisConstant.NEWS_METRICS_PREFIX + event.getNewsId();
//
// 		final ActionType action = event.getAction();
// 		long remove = 0;
// 		// 如果是支持或反对操作，则需要先检查是否存在互斥操作（即假如同一用户对同一新闻之前进行了反对操作，现在要进行支持操作，则需要先取消之前的反对操作）
// 		switch (action) {
// 			case SUPPORT:
// 				remove = redisUtil.sRemove(RedisConstant.USER_NEWS_ACTION_PREFIX + event.getUserId() + ":" + ActionType.OPPOSE.getField(), event.getNewsId());
// 				if (remove > 0) {
// 					redisUtil.hincr(metricsKey, ActionType.OPPOSE.getField(), -1);
// 				}
// 				break;
// 			case OPPOSE:
// 				remove = redisUtil.sRemove(RedisConstant.USER_NEWS_ACTION_PREFIX + event.getUserId() + ":" + ActionType.SUPPORT.getField(), event.getNewsId());
// 				if (remove > 0) {
// 					redisUtil.hincr(metricsKey, ActionType.SUPPORT.getField(), -1);
// 				}
// 				break;
// 		}
//
// 		// 更新Hash中的新闻指标（浏览量、评论数、收藏数等）
// 		updateMetrics(metricsKey, event);
//
// 		// 更新用户新闻行为记录
// 		updateUserNewsAction(event);
//
// 		// 更新热度
// 		updateHotness(metricsKey, event);
// 	}
//
// 	// 更新新闻指标（浏览量、评论数、收藏数等）
// 	private void updateMetrics(String metricsKey, UserActionEvent event) {
// 		int delta = isCancelAction(event.getAction()) ? -1 : 1;
// 		redisUtil.hincr(metricsKey, event.getAction().getField(), delta);
// 	}
//
// 	private void updateHotness(String metricsKey, UserActionEvent event) {
// 		// 计算当前热度分
// 		Map<Object, Object> metrics = redisUtil.hmget(metricsKey);
// 		double score = calculateHotness(metrics);
//
// 		// 更新1小时窗口Sorted Set（过期时间25小时）
// 		String hourKey = getCurrentHourKey();
// 		redisUtil.zAdd(hourKey, event.getNewsId(), score);
// 		redisUtil.expire(hourKey, 25, TimeUnit.HOURS);
// 	}
//
// 	/**
// 	 * 更新用户新闻行为记录（即用户对某一新闻是否进行了支持、反对、收藏等操作，以及取消支持、取消反对、取消收藏等操作）
// 	 * 记录格式：user_news_action:userId:action newsId，如user_news_action:1:supports 1表示用户1对新闻1进行了支持
// 	 * 存储类型为 Redis Set，以便快速查询某个用户是否对某一新闻进行过某种操作
// 	 * 用来判断某个用户是否对某一新闻进行过某种操作（如是否对某一新闻进行了支持、反对、收藏等操作）
// 	 */
// 	private void updateUserNewsAction(UserActionEvent event) {
// 		ActionType action = event.getAction();
// 		// 如果是取消操作，则需要从Redis Set中删除该记录
// 		if (isCancelAction(action)) {
// 			action = ActionType.from(action.name().substring(2));
// 			// 取消用户对某一新闻的支持、反对、收藏等操作，则需要从Redis Set中删除该记录
// 			String actionKey = RedisConstant.USER_NEWS_ACTION_PREFIX + event.getUserId() + ":" + action.getField();
// 			redisUtil.sRemove(actionKey, event.getNewsId());
// 			return;
// 		}
//
// 		// 如果不是取消操作，则需要将该记录存储到Redis Set中
// 		String actionKey = RedisConstant.USER_NEWS_ACTION_PREFIX + event.getUserId() + ":" + action.getField();
// 		redisUtil.sSet(actionKey, event.getNewsId());
// 	}
//
// 	// 判断是否为取消操作
// 	private boolean isCancelAction(ActionType action) {
// 		return action.name().startsWith("UN");
// 	}
//
// 	private String getCurrentHourKey() {
// 		return RedisConstant.HOT_NEWS_HOUR_PREFIX + HotNewsUtil.getCurrentHour();
// 	}
//
// 	// 计算热度分
// 	private double calculateHotness(Map<Object, Object> metrics) {
// 		// 计算基础热度
// 		double base = calculateBaseHotness(metrics);
// 		long publishTime = (long) metrics.get("publishTime");
// 		// 计算衰减热度
// 		return calculateDecayedHotness(base, publishTime);
// 	}
//
//
// 	// 基础热度计算
// 	private double calculateBaseHotness(Map<Object, Object> metrics) {
// 		return ActionType.calculateHotScore(metrics);
// 	}
//
// 	// 计算热度衰减率
// 	private double calculateDecayRate(long deltaHours) {
// 		if (deltaHours < 1) return HotNewsUtil.DECAY_1HR;
// 		else if (deltaHours < 24) return HotNewsUtil.DECAY_24HR;
// 		else if (deltaHours < 24 * 3) return HotNewsUtil.DECAY_3DAYS;
// 		else if (deltaHours < 24 * 5) return HotNewsUtil.DECAY_5DAYS;
// 		else return HotNewsUtil.DECAY_AFTER_5DAYS;
// 	}
//
// 	/**
// 	 * 时间衰减计算
// 	 * 计算公式举例：热度 = [1.5 × log(浏览量+1) + 0.25 × 点赞数 + 0.35 × 评论数 + 0.25 × 收藏数] × e^(-λt)
// 	 * 其中λ为衰减速率，t为时间间隔（单位：小时）
// 	 */
// 	private double calculateDecayedHotness(double base, long publishTime) {
// 		long deltaHours = (System.currentTimeMillis() - publishTime) / 3600_000;
// 		double delta = calculateDecayRate(deltaHours);
// 		return base * Math.exp(-delta * deltaHours); // λ=delta, t=deltaHours
// 	}
// }