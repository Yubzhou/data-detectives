package com.yubzhou.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubzhou.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.nio.charset.StandardCharsets;

@RestControllerAdvice
@Slf4j
public class CustomResponseBodyAdvice implements ResponseBodyAdvice<Object> {
	// 注入 Jackson ObjectMapper
	private final ObjectMapper objectMapper;
	// 注入 HttpServletResponse，用于获取HTTP状态码
	private final HttpServletResponse httpServletResponse;

	// 定义application/json;charset=UTF-8 MediaType
	private static final MediaType APPLICATION_JSON_UTF8;

	static {
		APPLICATION_JSON_UTF8 = new MediaType("application", "json", StandardCharsets.UTF_8);
	}

	@Autowired
	public CustomResponseBodyAdvice(ObjectMapper objectMapper, HttpServletResponse httpServletResponse) {
		this.objectMapper = objectMapper;
		this.httpServletResponse = httpServletResponse;
	}

	@Override
	public boolean supports(MethodParameter returnType,
							Class<? extends HttpMessageConverter<?>> converterType) {
		// 如果返回类型为Result，则直接返回false，不进行任何处理
		return !Result.class.isAssignableFrom(returnType.getParameterType());
	}

	@Override
	public Object beforeBodyWrite(
			Object body,
			MethodParameter returnType,
			MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType,
			ServerHttpRequest request,
			ServerHttpResponse response) {
		// 能进入此方法说明 body 不是 Result 类型，因此需要进行统一返回结果处理

		// 记录日志
		log.info("CustomResponseBodyAdvice处理，返回类型：{}，body值类型：{}", returnType.getParameterType(), body == null ? "null" : body.getClass());

		// 如果body的类型为Result，则直接返回（如：可能使用了 ResponseEntity 包装了 Result 对象）
		if (body instanceof Result) {
			return body;
		}

		// 此时body的类型不是Result，因此需要构造Result对象，且根据HTTP状态码进行特殊处理（使Result对象里面的code字段与HTTP状态码一致）
		// 基础结果对象构造
		Result<Object> result = buildResultObject(body);

		// 特殊处理String类型返回值
		// 如果返回类型为String，则将其包装为Result对象并手动转为JSON字符串返回
		// 当返回值为 String 类型时，默认情况下会使用 StringHttpMessageConverter，其 Content-Type 会被设置为 text/html;charset=UTF-8
		if (body instanceof String) {
			return handleStringResponse(result, response);
		}

		// 如果返回类型不是String，则直接对象类型返回，Spring会自动序列化为JSON字符串
		return result;
	}

	/**
	 * 构建基础结果对象（使Result对象里面的code字段与HTTP状态码一致）
	 *
	 * @param body 返回值
	 * @return Result
	 */
	private Result<Object> buildResultObject(Object body) {
		if (httpServletResponse == null) {
			return Result.success(body);
		}

		// 记录日志
		log.info("CustomResponseBodyAdvice处理，HTTP状态码：{}", httpServletResponse.getStatus());

		// 根据HTTP状态码构造结果对象（使Result对象里面的code字段与HTTP状态码一致）
		// 如果响应码为200，则使用自定义success方法（里面有默认的消息），否则使用自定义of方法（message参数为null）
		int status = httpServletResponse.getStatus();
		return status == HttpStatus.OK.value()
				? Result.success(body)
				: Result.of(status, null, body);
	}

	/**
	 * 处理String类型返回值
	 *
	 * @param result   基础结果对象
	 * @param response ServerHttpResponse
	 * @return Object
	 */
	private Object handleStringResponse(Result<?> result, ServerHttpResponse response) {
		try {
			response.getHeaders().setContentType(APPLICATION_JSON_UTF8);
			return objectMapper.writeValueAsString(result);
		} catch (JsonProcessingException e) {
			log.error("String类型序列化失败", e);
			return Result.fail("响应处理异常：" + e.getMessage());
		}
	}
}
