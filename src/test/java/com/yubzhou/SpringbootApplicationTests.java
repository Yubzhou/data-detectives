package com.yubzhou;


import com.yubzhou.properties.AsyncProperties;
import com.yubzhou.properties.FileUploadProperties;
import com.yubzhou.util.PathUtil;
import com.yubzhou.util.SpelEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootApplicationTests {

	@Autowired
	private FileUploadProperties fileUploadProperties;

	@Autowired
	private AsyncProperties asyncProperties;

	@Autowired
	private SpelEvaluator spelEvaluator;

	@Test
	public void test01() throws Exception {
		String imagePath = PathUtil.getExternalPath(fileUploadProperties.getImage().getUploadDir()).toString();
		System.out.println(imagePath);
	}

	@Test
	public void test02() throws Exception {
		System.out.println(asyncProperties.getSse());
		System.out.println(asyncProperties.getUpload());
		System.out.println(asyncProperties.getGlobal());
	}

	@Test
	public void test03() throws Exception {
		String spelExpression = "T(java.lang.Math).PI";

		Double PI = spelEvaluator.evaluate(spelExpression, Double.class);
		System.out.println(PI);
		System.out.println(spelEvaluator.getCacheStats());
	}
}
