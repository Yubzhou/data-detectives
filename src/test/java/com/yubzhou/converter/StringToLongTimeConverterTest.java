package com.yubzhou.converter;

import com.yubzhou.properties.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StringToLongTimeConverterTest {

	private final JwtProperties jwtProperties;

	@Autowired
	StringToLongTimeConverterTest(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	@Test
	void autoConvert() {
		System.out.println(jwtProperties);
	}

}