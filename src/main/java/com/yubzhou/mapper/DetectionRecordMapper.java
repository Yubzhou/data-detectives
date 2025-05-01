package com.yubzhou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yubzhou.model.po.DetectionRecord;
import com.yubzhou.model.pojo.UserDetectionStats;
import com.yubzhou.model.vo.AchievementVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DetectionRecordMapper extends BaseMapper<DetectionRecord> {

	// 统计用户的最长连续检测天数（基于窗口函数）
	@Select("""
			SELECT IFNULL(MAX(`cont_days`), 0) AS `max_continuous_days`
			FROM (SELECT COUNT(*) AS `cont_days`
			FROM (SELECT `detection_date`,
						 DATE_SUB(`detection_date`, INTERVAL ROW_NUMBER() OVER (ORDER BY `detection_date`) DAY) AS `grp`
				  FROM (SELECT DISTINCT `detection_date`
						FROM `detection_records`
						WHERE `user_id` = #{userId}) AS `dedup`) AS `t`
			GROUP BY `grp`) AS `t2`;
			""")
	Integer getMaxContinuousDays(@Param("userId") Long userId);

	// 查询用户的总检测次数、检测结果为真新闻的数量、今年检测总天数
	@Select("""
			SELECT COUNT(*)                    AS `total_detections`,
			       SUM(`detection_result` = 1) AS `true_news_count`,
			       COUNT(DISTINCT CASE
			       		WHEN `created_at` >= MAKEDATE(YEAR(NOW()), 1)
			            THEN DATE(`created_at`)
			           END)                    AS `total_detection_days`
			FROM `detection_records`
			WHERE `user_id` = #{userId};
			""")
	AchievementVo getAchievement(@Param("userId") Long userId);

	// 统计用户的历史最长连续检测天数、当前连续检测天数、最后一次检测日期（yyyy-MM-dd）
	@Select(
			"""
			WITH
			-- 获取用户所有唯一检测日期
			dedup AS (
				SELECT DISTINCT detection_date
				FROM detection_records
				WHERE user_id = #{userId}
			),
			-- 对日期进行分组，标记连续日期
			grouped AS (
				SELECT
					detection_date,
					DATE_SUB(detection_date, INTERVAL ROW_NUMBER() OVER (ORDER BY detection_date) DAY) AS grp
				FROM dedup
			),
			-- 统计每个连续分组的持续天数及起止日期
			group_summary AS (
				SELECT
					grp,
					COUNT(*) AS cont_days,
					MIN(detection_date) AS start_date,
					MAX(detection_date) AS end_date
				FROM grouped
				GROUP BY grp
			),
			-- 计算最长连续天数
			max_continuous AS (
				SELECT MAX(cont_days) AS max_continuous_days
				FROM group_summary
			),
			-- 获取最近一次检测日期
			last_date AS (
				SELECT MAX(detection_date) AS last_detection_date
				FROM dedup
			),
			-- 获取当前连续天数（基于最近日期所在分组），直接排序取当前连续天数
			current_group AS (
				SELECT cont_days AS current_continuous_days
				FROM group_summary
				ORDER BY end_date DESC
				LIMIT 1
			)
			-- 组合最终结果
			SELECT
				COALESCE(mc.max_continuous_days, 0) AS max_continuous_days, -- 当前没有最长连续天数时，返回0
				ld.last_detection_date,
				COALESCE(cg.current_continuous_days, 0) AS current_continuous_days -- 当前没有连续天数时，返回0
			FROM max_continuous mc
					 CROSS JOIN last_date ld
					 LEFT JOIN current_group cg ON TRUE;
			"""
	)
	UserDetectionStats getDetectionStats(@Param("userId") Long userId);
}

