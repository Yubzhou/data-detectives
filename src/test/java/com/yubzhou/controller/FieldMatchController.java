package com.yubzhou.controller;

import com.yubzhou.annotation.JwtIgnore;
import com.yubzhou.pojo.fieldmatch.ContactForm;
import com.yubzhou.pojo.fieldmatch.RegistrationForm1;
import com.yubzhou.pojo.fieldmatch.RegistrationForm2;
import com.yubzhou.pojo.fieldmatch.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fieldmatch")
@Validated
@JwtIgnore
public class FieldMatchController {
	// 简单字段校验
	@PostMapping("/match01")
	public void match01(@Valid @RequestBody RegistrationForm1 form) {
		System.out.println("match01: " + form);
	}

	// 复杂对象校验
	@PostMapping("/match02")
	public void match02(@Valid @RequestBody RegistrationForm2 form) {
		System.out.println("match02: " + form);
	}


	// 复杂对象嵌套路径校验
	@PostMapping("/match03")
	public void match03(@Valid @RequestBody UserUpdateRequest request) {
		System.out.println("match03: " + request);
	}

	// 多组校验
	@PostMapping("/match04")
	public void match04(@Valid @RequestBody ContactForm form) {
		System.out.println("match04: " + form);
	}
}
