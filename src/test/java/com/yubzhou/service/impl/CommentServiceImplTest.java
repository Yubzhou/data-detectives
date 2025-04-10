package com.yubzhou.service.impl;

import com.yubzhou.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommentServiceImplTest {

	@Autowired
	private CommentService commentService;

	@Test
	public void test() throws Exception {
		// 只有影响行数大于 0 的时候才算成功
		boolean deleted = commentService.deleteComment(1L, 1L);
		System.out.println(deleted);
	}

}