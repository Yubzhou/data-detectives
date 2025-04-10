package com.yubzhou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yubzhou.model.po.Comment;
import org.apache.ibatis.annotations.Update;


public interface CommentMapper extends BaseMapper<Comment> {

	// 一次性将评论表的某一新闻的评论数同步到新闻表中
	@Update("""
			UPDATE `news` `n`
			SET `n`.`comments` = (
			  SELECT COUNT(*)
			  FROM `comments` `c`
			  WHERE `c`.`news_id` = `n`.`id`
			);
			""")
	void syncCommentsCount();

}

