package com.yubzhou.controller;

import com.yubzhou.annotation.JwtIgnore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@JwtIgnore
public class IndexController {

	@GetMapping("/")
	public String index() {
		return "欢迎来到NFact平台的后端服务首页";
	}

	@GetMapping("/api")
	public String apiIndex() {
		return "欢迎使用NFact平台的后端API接口";
	}
}
