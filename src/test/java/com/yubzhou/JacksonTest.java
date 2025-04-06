package com.yubzhou;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.util.DateTimeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@SpringBootTest
public class JacksonTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ObjectMapper defaultObjectMapper;

	@Autowired
	@Qualifier("businessObjectMapper")
	private ObjectMapper businessObjectMapper;

	@Test
	public void test01() throws Exception {
		System.out.println("defaultObjectMapper: " + defaultObjectMapper);
		System.out.println("businessObjectMapper: " + businessObjectMapper);
		System.out.println(applicationContext.getBeansOfType(ObjectMapper.class));
	}

	@Test
	public void test02() throws Exception {
		// 分别对Date、LocalDateTime、Instant、ZonedDateTime、OffsetDateTime进行序列化测试
		Date date = new Date();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.writeValueAsString(date));
		System.out.println("businessObjectMapper: " + businessObjectMapper.writeValueAsString(date));

		LocalDateTime localDateTime = LocalDateTime.now();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.writeValueAsString(localDateTime));
		System.out.println("businessObjectMapper: " + businessObjectMapper.writeValueAsString(localDateTime));

		Instant instant = Instant.now();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.writeValueAsString(instant));
		System.out.println("businessObjectMapper: " + businessObjectMapper.writeValueAsString(instant));

		ZonedDateTime zonedDateTime = ZonedDateTime.now();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.writeValueAsString(zonedDateTime));
		System.out.println("businessObjectMapper: " + businessObjectMapper.writeValueAsString(zonedDateTime));

		OffsetDateTime offsetDateTime = OffsetDateTime.now();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.writeValueAsString(offsetDateTime));
		System.out.println("businessObjectMapper: " + businessObjectMapper.writeValueAsString(offsetDateTime));
	}

	@Test
	public void test03() throws Exception {
		// 分别对Date、LocalDateTime、Instant、ZonedDateTime、OffsetDateTime进行反序列化测试
		Date date = new Date();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.readValue(defaultObjectMapper.writeValueAsString(date), Date.class));
		System.out.println("businessObjectMapper: " + businessObjectMapper.readValue(businessObjectMapper.writeValueAsString(date), Date.class));

		LocalDateTime localDateTime = LocalDateTime.now();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.readValue(defaultObjectMapper.writeValueAsString(localDateTime), LocalDateTime.class));
		System.out.println("businessObjectMapper: " + businessObjectMapper.readValue(businessObjectMapper.writeValueAsString(localDateTime), LocalDateTime.class));

		Instant instant = Instant.now();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.readValue(defaultObjectMapper.writeValueAsString(instant), Instant.class));
		System.out.println("businessObjectMapper: " + businessObjectMapper.readValue(businessObjectMapper.writeValueAsString(instant), Instant.class));

		ZonedDateTime zonedDateTime = ZonedDateTime.now();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.readValue(defaultObjectMapper.writeValueAsString(zonedDateTime), ZonedDateTime.class));
		System.out.println("businessObjectMapper: " + businessObjectMapper.readValue(businessObjectMapper.writeValueAsString(zonedDateTime), ZonedDateTime.class));

		OffsetDateTime offsetDateTime = OffsetDateTime.now();
		System.out.println("defaultObjectMapper: " + defaultObjectMapper.readValue(defaultObjectMapper.writeValueAsString(offsetDateTime), OffsetDateTime.class));
		System.out.println("businessObjectMapper: " + businessObjectMapper.readValue(businessObjectMapper.writeValueAsString(offsetDateTime), OffsetDateTime.class));

	}

	@Test
	public void test04() throws Exception {
		System.out.println(DateTimeUtil.format(Instant.now(), DateTimeUtil.GLOBAL_DATE_TIME_NO_MILLIS_FORMATTER));
	}


}
