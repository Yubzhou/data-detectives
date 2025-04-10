package com.yubzhou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yubzhou.model.po.DetectionRecord;
import com.yubzhou.model.vo.DetectionStatsVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DetectionRecordMapper extends BaseMapper<DetectionRecord> {

	@Select("""
			-- 统计用户的总检测次数和最长连续检测天数（基于窗口函数）
			SELECT (SELECT COUNT(*) FROM `detection_records` WHERE `user_id` = #{userId}) AS `total_detections`,
			       IFNULL(MAX(`cont_days`), 0)                                    AS `max_continuous_days`
			FROM (SELECT COUNT(*) AS `cont_days`
			      FROM (SELECT `user_id`,
			                   `detection_date`,
			                   DATE_SUB(`detection_date`, INTERVAL ROW_NUMBER() OVER (ORDER BY `detection_date`) DAY) AS `grp`
			            FROM (SELECT DISTINCT `user_id`, `detection_date`
			                  FROM `detection_records`
			                  WHERE `user_id` = #{userId}) AS `dedup`) AS `t`
			      GROUP BY `grp`) AS `t2`;
			""")
	DetectionStatsVo selectDetectionStats(@Param("userId") Long userId);

}

