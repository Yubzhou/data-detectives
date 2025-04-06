package com.yubzhou.config;

import com.yubzhou.exception.TokenInvalidException;
import com.yubzhou.interceptor.JwtAuthInterceptor;
import com.yubzhou.interceptor.TimeZoneInterceptor;
import com.yubzhou.properties.FileUploadProperties;
import com.yubzhou.util.JwtUtil;
import com.yubzhou.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class GlobalWebMvcConfig implements WebMvcConfigurer {
	// 注入自定义的权限拦截器JwtAuthInterceptor所需的JwtUtil对象
	private final JwtUtil jwtUtil;

	private final FileUploadProperties fileUploadProperties;

	/**
	 * 重写父类提供的跨域请求处理的接口
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
				// 设置允许跨域请求的路径
				.addMapping("/**") // 允许所有请求路径
				// .addMapping("/api/**") // 只允许 /api 开头的请求路径

				// 设置允许跨域请求的域名
				// .allowedOrigins("*") // 允许所有域名跨域请求
				.allowedOrigins(
						"http://localhost:8080",
						"http://127.0.0.1:5500",
						"http://127.0.0.1:5173",
						"http://localhost:8081",
						"http://localhost:5173",
						"http://localhost:63342" // idea 内置浏览器访问
				) // 只允许本机跨域请求

				// 设置允许跨域请求的请求方法
				// .allowedMethods("*") // 允许所有请求方法
				.allowedMethods(
						"GET",
						"POST",
						"PUT",
						"DELETE",
						"PATCH",
						"OPTIONS",
						"HEAD"
				) // 允许指定请求方法

				// 设置允许跨域请求的请求头
				// .allowedHeaders("*") // 允许所有请求头
				.allowedHeaders(
						"Content-Type",
						"Authorization",
						"X-Requested-With",
						"X-Forwarded-For",
						"Proxy-Client-IP",
						"WL-Proxy-Client-IP",
						"X-Time-Zone" // 自定义请求头
				) // 只允许Content-Type、X-Requested-With、Authorization 和 X-Timezone 请求头
				// X-Timezone 请求头用于设置时区（其中X-Timezone为自定义请求头）

				// 设置是否允许浏览器获取响应头，Access-Control-Expose-Headers
				.exposedHeaders(
						TokenInvalidException.TOKEN_INVALID_HEADER // 设置自定义请求头X-Token-Invalid，用于标识token无效
				)

				// 设置是否允许携带cookie
				.allowCredentials(true) // 允许携带cookie

				// 设置预检请求的有效期（Access-Control-Max-Age）
				.maxAge(3600); // 预检请求的有效期为1小时（单位为秒）
	}

	/**
	 * 重写父类提供的拦截器注册接口
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// addInterceptor：添加拦截器
		// addPathPatterns：添加拦截路径
		// excludePathPatterns：排除拦截路径

		// 如果未指定order，则order默认为0
		// 如果order相同，则按添加的先后顺序执行（先添加的先执行）

		// 添加时区拦截器
		registry.addInterceptor(new TimeZoneInterceptor())
				.addPathPatterns("/**");


		// 添加权限拦截器
		registry.addInterceptor(new JwtAuthInterceptor(jwtUtil))
				.addPathPatterns("/**")
				.excludePathPatterns(
						"/static/**", // 静态资源
						"/**/favicon.ico", // 网站图标
						"/mytest/**",
						"/druid/**"
				);
	}

	/**
	 * 手动配置静态资源路径
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// addResourceHandler：添加URL映射地址
		// addResourceLocations：添加资源实际目录

		// 写在前面的路径优先级更高，因此会覆盖后面的路径

		// // 暴露 static/root-files/ 目录下的所有文件到根路径下
		// registry.addResourceHandler("/*")  // 匹配根路径请求
		// 		.addResourceLocations("classpath:/static/html/");

		// // 默认静态资源需通过 /static/ 访问
		// registry.addResourceHandler("/static/**")
		// 		.addResourceLocations("classpath:/static/");

		// 默认静态资源需通过 / 访问
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/static/");

		// 图片存储路径
		String imagePath = PathUtil.getExternalPath(fileUploadProperties.getImage().getUploadDir()).toString();
		registry.addResourceHandler("/uploads/images/**")
				.addResourceLocations("file:" + imagePath + "/")
				.setCachePeriod(3600);
	}
}