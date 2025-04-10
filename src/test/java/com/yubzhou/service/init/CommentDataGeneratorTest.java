package com.yubzhou.service.init;

import com.yubzhou.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentDataGeneratorTest {

	@Autowired
	private CommentDataGenerator commentDataGenerator;
	@Autowired
	private CommentService commentService;

	@Test
	public void testGenerateData() throws Exception {
		commentDataGenerator.run();
	}

	// 一次性将评论表的某一新闻的评论数同步到新闻表中
	@Test
	public void testSyncCommentCount() {
		commentService.syncCommentCount();
	}


}