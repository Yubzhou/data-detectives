package com.yubzhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubzhou.mapper.DetectionRecordMapper;
import com.yubzhou.model.dto.CreateDetectionRecordDto;
import com.yubzhou.model.po.DetectionRecord;
import com.yubzhou.model.pojo.UserDetectionStats;
import com.yubzhou.model.vo.AchievementVo;
import com.yubzhou.service.DetectionRecordService;
import com.yubzhou.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionRecordServiceImpl
		extends ServiceImpl<DetectionRecordMapper, DetectionRecord>
		implements DetectionRecordService {

	private final RedisUtil redisUtil;

	private static final String CACHE_KEY_PREFIX = "user:detection:stats:";
	private static final long CACHE_TTL_HOURS = 24;

	/**
	 * 根据用户ID、时间范围、检测类型和检测结论筛选记录（分页）
	 *
	 * @param userId          用户ID（必选）
	 * @param days            时间范围（单位：天，例如 7 表示最近7天，可选）
	 * @param detectionType   检测类型（0: 高效，1: 高精度，可选）
	 * @param detectionResult 检测结论（true/false，可选）
	 * @param pageNum         当前页码（从1开始，必选）
	 * @param pageSize        每页数量（可选）
	 * @return 分页结果
	 */
	@Override
	public IPage<DetectionRecord> queryByUnionIndex(
			Long userId,
			Integer days,
			Integer detectionType,
			Boolean detectionResult,
			Integer pageNum,
			Integer pageSize) {

		pageNum = pageNum == null ? 1 : pageNum;
		pageSize = pageSize == null ? 10 : pageSize;

		LocalDateTime endTime = null, startTime = null;

		// 计算时间范围
		if (days != null) {
			endTime = LocalDateTime.now(); // 当前时间
			startTime = endTime.minusDays(days); // 开始时间 = 当前时间 - days天
		}

		// 创建分页对象
		Page<DetectionRecord> page = new Page<>(pageNum, pageSize);

		// 构建查询条件（使用 LambdaQueryWrapper）
		LambdaQueryWrapper<DetectionRecord> wrapper = Wrappers.lambdaQuery();

		wrapper.eq(DetectionRecord::getUserId, userId) // 必须要指定 user_id
				.ge(days != null, DetectionRecord::getCreatedAt, startTime)
				.le(days != null, DetectionRecord::getCreatedAt, endTime)
				.eq(detectionType != null, DetectionRecord::getDetectionType, detectionType)
				.eq(detectionResult != null, DetectionRecord::getDetectionResult, detectionResult)
				.orderByDesc(DetectionRecord::getCreatedAt);

		// 执行分页查询（使用联合索引字段）
		return page(page, wrapper);
	}

	// 获取用户的最长连续检测天数（从数据库中获取）
	@Override
	public Integer getMaxContinuousDaysFromDB(long userId) {
		return this.baseMapper.getMaxContinuousDays(userId);
	}

	// 获取用户的最长连续检测天数（从缓存中获取）
	@Override
	public Long getMaxContinuousDaysFromCache(long userId) {
		return this.getDetectionStats(userId).getMaxContinuousDays();
	}

	// 获取用户的总检测次数、检测结果为真新闻的数量、今年检测总天数
	@Override
	public AchievementVo getAchievement(long userId) {
		return this.baseMapper.getAchievement(userId);
	}

	/**
	 * 处理检测结果
	 *
	 * @param userId  用户ID
	 * @param dtoList 待处理的检测记录列表
	 * @return 是否处理成功
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean processDetection(Long userId, List<CreateDetectionRecordDto> dtoList) {
		// 1. 保存检测记录到数据库
		boolean b = saveDetectionRecordsToDB(userId, dtoList);

		// 2. 更新统计信息缓存
		updateDetectionStatsCache(userId);

		return b;
	}

	/**
	 * 批量保存检测记录
	 *
	 * @param userId  用户ID
	 * @param dtoList 待保存的检测记录列表
	 * @return 是否保存成功
	 */
	private boolean saveDetectionRecordsToDB(Long userId, List<CreateDetectionRecordDto> dtoList) {
		java.util.List<DetectionRecord> records = dtoList.stream().map(dto -> dto.toEntity(userId)).toList();
		return this.saveBatch(records, this.DEFAULT_BATCH_SIZE);
	}

	// /**
	//  * 更新用户的检测统计信息缓存
	//  *
	//  * @param userId 用户ID
	//  */
	// private void updateDetectionStatsCache(Long userId) {
	// 	String cacheKey = buildRedisKey(userId);
	//
	// 	// 尝试从缓存获取
	// 	UserDetectionStats cachedStats = redisUtil.get(cacheKey, UserDetectionStats.class);
	// 	LocalDate today = LocalDate.now();
	//
	// 	if (cachedStats != null) {
	// 		// 缓存存在时的处理逻辑
	//
	// 		// lastDate有可能为空（当用户不存在检测记录时）
	// 		LocalDate lastDate = cachedStats.getLastDetectionDate();
	//
	// 		// 如果是今天，则不处理（因为之前已经处理过了）
	// 		if (isSameDate(lastDate, today)) return;
	//
	// 		// 如果是连续检测，则更新当前连续天数
	// 		if (isSameDate(lastDate, today.minusDays(1))) {
	// 			// 连续检测：当前天数+1
	// 			cachedStats.setCurrentContinuousDays(cachedStats.getCurrentContinuousDays() + 1);
	// 		} else {
	// 			// 非连续，重置当前天数（当天多次检测只算一次）
	// 			cachedStats.setCurrentContinuousDays(1L);
	// 		}
	// 		// 设置最后检测日期
	// 		cachedStats.setLastDetectionDate(today);
	// 		// 检查是否需要更新最长检测天数
	// 		if (cachedStats.getCurrentContinuousDays() > cachedStats.getMaxContinuousDays()) {
	// 			cachedStats.setMaxContinuousDays(cachedStats.getCurrentContinuousDays());
	// 		}
	//
	// 		// 更新缓存
	// 		redisUtil.set(cacheKey, cachedStats, CACHE_TTL_HOURS, TimeUnit.HOURS);
	// 		log.info("更新用户检测统计信息缓存（缓存存在）：{}", cachedStats);
	// 	} else {
	// 		// 缓存不存在，从数据库加载
	// 		UserDetectionStats dbStats = this.baseMapper.getDetectionStats(userId);
	// 		if (dbStats == null) {
	// 			dbStats = UserDetectionStats.createDefault();
	//
	// 			// 设置默认值并处理新检测
	// 			LocalDate lastDate = dbStats.getLastDetectionDate();
	// 			if (isSameDate(lastDate, today.minusDays(1))) {
	// 				dbStats.setCurrentContinuousDays(dbStats.getCurrentContinuousDays() + 1);
	// 			} else {
	// 				dbStats.setCurrentContinuousDays(1L);
	// 			}
	// 			// 设置最后检测日期
	// 			dbStats.setLastDetectionDate(today);
	// 			// 检查是否需要更新最长检测天数
	// 			if (dbStats.getCurrentContinuousDays() > dbStats.getMaxContinuousDays()) {
	// 				dbStats.setMaxContinuousDays(dbStats.getCurrentContinuousDays());
	// 			}
	// 		}
	// 		// 保存到缓存
	// 		redisUtil.set(cacheKey, dbStats, CACHE_TTL_HOURS, TimeUnit.HOURS);
	// 		log.info("更新用户检测统计信息缓存（缓存不存在，从数据库获取）：{}", dbStats);
	// 	}
	// }

	/**
	 * 更新用户的检测统计信息缓存
	 *
	 * @param userId 用户ID
	 */
	private void updateDetectionStatsCache(Long userId) {
		String cacheKey = buildRedisKey(userId);
		LocalDate today = LocalDate.now();

		// 尝试从缓存获取
		UserDetectionStats stats = redisUtil.get(cacheKey, UserDetectionStats.class);
		boolean cacheExists = (stats != null);

		// 缓存不存在时从数据库加载或创建默认对象
		if (!cacheExists) {
			stats = Optional.ofNullable(this.baseMapper.getDetectionStats(userId))
					.orElseGet(UserDetectionStats::createDefault);
		}

		// 最后检测日期为今天时，只维护缓存不更新统计
		if (isSameDate(stats.getLastDetectionDate(), today)) {
			if (!cacheExists) {
				redisUtil.set(cacheKey, stats, CACHE_TTL_HOURS, TimeUnit.HOURS);
				log.info("刷新缓存[最后检测日为当天] userId:{} stats:{}", userId, stats);
			}
			return;
		}

		// 执行统计信息更新
		updateStatsForNewDay(stats, today);

		// 更新/设置缓存
		redisUtil.set(cacheKey, stats, CACHE_TTL_HOURS, TimeUnit.HOURS);
		log.info("更新统计缓存[{}] userId:{} stats:{}",
				cacheExists ? "缓存已存在" : "新建缓存", userId, stats);
	}

	/**
	 * 处理新一天的统计逻辑（非线程安全）
	 *
	 * @param stats 统计对象
	 * @param today 当天日期
	 */
	private void updateStatsForNewDay(UserDetectionStats stats, LocalDate today) {
		LocalDate lastDate = stats.getLastDetectionDate();

		// 判断连续检测状态
		if (isSameDate(lastDate, today.minusDays(1))) {
			stats.setCurrentContinuousDays(stats.getCurrentContinuousDays() + 1);
		} else {
			stats.setCurrentContinuousDays(1L);
		}

		// 更新最后检测日期
		stats.setLastDetectionDate(today);

		// 更新最长连续检测天数
		if (stats.getCurrentContinuousDays() > stats.getMaxContinuousDays()) {
			stats.setMaxContinuousDays(stats.getCurrentContinuousDays());
		}
	}

	// 判断是否是相同的日期
	private boolean isSameDate(LocalDate date1, LocalDate date2) {
		return Objects.equals(date1, date2);
	}

	/**
	 * 获取用户的检测统计信息（带缓存）
	 *
	 * @param userId 用户ID
	 * @return 用户检测统计信息
	 */
	public UserDetectionStats getDetectionStats(Long userId) {
		String cacheKey = buildRedisKey(userId);
		UserDetectionStats cachedStats = redisUtil.get(cacheKey, UserDetectionStats.class);
		if (cachedStats != null) {
			log.info("从缓存获取用户检测统计信息：{}", cachedStats);
			return cachedStats;
		}

		// 缓存未命中，从数据库加载
		UserDetectionStats dbStats = this.baseMapper.getDetectionStats(userId);
		if (dbStats == null) {
			dbStats = UserDetectionStats.createDefault();
		}

		// 设置缓存
		redisUtil.set(cacheKey, dbStats, CACHE_TTL_HOURS, TimeUnit.HOURS);
		log.info("从数据库获取用户检测统计信息：{}", dbStats);
		return dbStats;
	}

	// 构建redis key
	private String buildRedisKey(Long userId) {
		return CACHE_KEY_PREFIX + userId;
	}
}
