package com.yubzhou.config;

import com.yubzhou.exception.TokenInvalidException;
import com.yubzhou.interceptor.AdminAuthInterceptor;
import com.yubzhou.interceptor.JwtAuthInterceptor;
import com.yubzhou.interceptor.TimeZoneInterceptor;
import com.yubzhou.properties.CorsProperties;
import com.yubzhou.properties.FileUploadProperties;
import com.yubzhou.util.JwtUtil;
import com.yubzhou.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class GlobalWebMvcConfig implements WebMvcConfigurer {
	// 注入自定义的权限拦截器JwtAuthInterceptor所需的JwtUtil对象
	private final JwtUtil jwtUtil;
	private final FileUploadProperties fileUploadProperties;
	private final CorsProperties corsProperties;

	/**
	 * 重写父类提供的跨域请求处理的接口
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
				// 设置允许跨域请求的路径
				.addMapping("/**") // 允许所有请求路径
				// .addMapping("/api/**") // 只允许 /api 开头的请求路径

				.allowedOriginPatterns(
						corsProperties.getAllowedOriginPatterns()
				)

				// // 设置允许跨域请求的域名
				// // .allowedOrigins("*") // 允许所有域名跨域请求
				// .allowedOrigins(
				// 		"http://localhost:8080",
				// 		"http://127.0.0.1:5500",
				// 		"http://127.0.0.1:5173",
				// 		"http://localhost:8081",
				// 		"http://localhost:5173",
				// 		"http://localhost:63342" // idea 内置浏览器访问
				// ) // 只允许本机跨域请求

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
				.addPathPatterns("/**")
				.order(1); // 数字越小优先级越高

		// 添加权限拦截器
		registry.addInterceptor(new JwtAuthInterceptor(jwtUtil))
				.addPathPatterns("/**")
				.excludePathPatterns(
						"/static/**", // 静态资源
						"/**/favicon.ico", // 网站图标
						"/mytest/**",
						"/druid/**"
				)
				.order(2);

		registry.addInterceptor(new AdminAuthInterceptor())
				.addPathPatterns("/private/**")      // 拦截所有管理接口
				.order(3);                          // 设置拦截器顺序（在权限拦截器之后）
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

		CacheControl cacheControl = CacheControl.maxAge(365, TimeUnit.DAYS)
				.cachePublic() // 表明响应可以被任何对象（包括：发送请求的客户端，代理服务器，等等）缓存
				.immutable() // 表示响应正文不会随时间而改变。资源（如果未过期）在服务器上不发生改变，因此客户端不应发送重新验证请求头（例如If-None-Match或 If-Modified-Since）来检查更新，即使用户显式地刷新页面。
				.mustRevalidate(); // 一旦资源过期（比如已经超过max-age），在成功向原始服务器验证之前，缓存不能用该资源响应后续请求。

		// 默认资源路径（与项目根路径同级）（外部目录，长期缓存）
		String defaultPath = PathUtil.getExternalPath("./default").toString();
		registry.addResourceHandler("/default/**")
				.addResourceLocations("file:" + defaultPath + "/")
				.setCacheControl(cacheControl); // 设置 HTTP 缓存控制头

		// 上传图片存储路径（外部目录，短期缓存）
		String imagePath = PathUtil.getExternalPath(fileUploadProperties.getImage().getUploadDir()).toString();
		registry.addResourceHandler("/uploads/images/**")
				.addResourceLocations("file:" + imagePath + "/")
				// 因为头像文件名使用唯一UUID，每次更新头像生成新UUID，旧资源不会变更。建议调整为长期缓存
				// UUID 保证资源唯一性，适合不可变
				.setCacheControl(cacheControl);

		CacheControl staticCacheControl = CacheControl.maxAge(1, TimeUnit.HOURS) // 设置缓存存储的最大周期，超过这个时间缓存被认为过期 (单位秒)。
				.cachePrivate() // 表明响应只能被单个用户缓存，不能作为共享缓存（即代理服务器不能缓存它）。私有缓存可以缓存响应内容，比如：对应用户的本地浏览器。
				.mustRevalidate(); // 一旦资源过期（比如已经超过max-age），在成功向原始服务器验证之前，缓存不能用该资源响应后续请求。

		// 默认静态资源需通过 / 访问（classpath:/static/，启用版本控制）
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/static/")
				.setCacheControl(staticCacheControl)
				.resourceChain(true) // 启用资源链优化
				.addResolver(new VersionResourceResolver().addContentVersionStrategy("/**")) // 启用版本控制（版本解析器优先）
				.addResolver(new PathResourceResolver()); // 添加路径资源解析器（基础路径解析器兜底）
	}
}