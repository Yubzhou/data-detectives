package com.yubzhou.service.impl;

import com.yubzhou.service.CommentService;
import io.lettuce.core.output.ListOfGenericMapsOutput;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class CommentServiceImplTest {

	@Autowired
	private CommentService commentService;

	@Test
	public void test() throws Exception {
		// 只有影响行数大于 0 的时候才算成功
		boolean deleted = commentService.deleteComment(1L, 1L);
		System.out.println(deleted);
	}

	@Test
	public void test02() throws Exception {
		// 获取指定用户的评论总数
		Long totalComments = commentService.getCommentCountByUserId(870L);
		log.info("用户[{}]的评论总数 = {}", 870, totalComments);

		// 获取指定新闻的评论总数
		totalComments = commentService.getCommentCountByNewsId(73L);
		log.info("新闻[{}]的评论总数 = {}", 73, totalComments);
	}

}