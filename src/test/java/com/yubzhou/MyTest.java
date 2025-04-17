package com.yubzhou;

import com.yubzhou.common.ReturnCode;
import com.yubzhou.common.UserActionEvent;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.properties.JwtTimeUnit;
import com.yubzhou.service.NewsService;
import com.yubzhou.util.ClientFingerprintUtil;
import com.yubzhou.util.PathUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MyTest {

	private String generateRandomString(int length) {
		// 范围为acsii可见字符 32-126（共95个，其中32为空格，126为~。如果不算32，共94个可见字符）
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int randomNum = random.nextInt(94) + 33;
			char randomChar = (char) randomNum;
			sb.append(randomChar);
		}
		return sb.toString();
	}

	@Test
	public void testGenerateRandomString() {
		String randomString = generateRandomString(32);
		System.out.println(randomString);
		System.out.println(randomString.length());
	}

	@Test
	public void test01() {
		// // 获取当前项目路径
		// System.out.println(System.getProperty("user.dir")); // D:\Codes\Intellij IDEA\spring\springboot-web-template

		Map<String, String> map = Map.of("key1", "value1", "key2", "value2");
		System.out.println(map.get("key1"));
		System.out.println(map.get("key2"));
		System.out.println(map.get("key3"));
	}

	@Test
	public void test02() {
		String source = "1000s";
		Pattern TIME_PATTERN = Pattern.compile("^(\\d+)(y|M|w|d|h|m|s|ms)$");
		Matcher matcher = TIME_PATTERN.matcher(source);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid time format: " + source);
		}

		long value = Long.parseLong(matcher.group(1));
		String unit = matcher.group(2);

		// 获取对应的枚举值
		JwtTimeUnit timeUnit = JwtTimeUnit.valueOf(unit);
		// 转换为毫秒值
		long millis = value * timeUnit.getMillis();
		System.out.println(millis);
	}

	@Test
	public void test03() {
		// 获取所有枚举常量的名称
		String timeUnits = Arrays.stream(JwtTimeUnit.values())
				.map(Enum::name)
				.collect(Collectors.joining("|"));

		System.out.println("Time Units: " + timeUnits);

		// 构建正则表达式
		String regex = "^(\\d+)(" + timeUnits + ")$";
		System.out.println("Generated Regex: " + regex);

		// 将正则表达式编译为Pattern对象
		Pattern timePattern = Pattern.compile(regex);
		System.out.println("Pattern: " + timePattern);
	}

	@Test
	public void test04() {
		String str = "UTC+07:00";
		ZoneId zoneId = ZoneId.of(str);
		ZonedDateTime now = ZonedDateTime.now();
		System.out.println("ZoneId: " + zoneId);
		System.out.println("Now: " + now);
		System.out.println(now.withZoneSameInstant(zoneId));
		// ZoneId.getAvailableZoneIds().forEach(System.out::println);
	}

	@Test
	public void test05() {
		String s1 = ClientFingerprintUtil.generate("127.0.0.1", "Apifox/1.0.0 (https://apifox.com)");
		String s2 = ClientFingerprintUtil.generate("127.0.0.1", "Apifox/1.0.0 (https://apifox.com)");
		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s1.equals(s2));
	}

	@Test
	public void test06() throws Exception {
		String regex = "^(?=.*\\d)(?=.*[a-zA-Z])[!-~]{8,30}$";
		Pattern pattern = Pattern.compile(regex);

		// 仅包含数字
		Matcher matcher = pattern.matcher("12345678");
		System.out.println("仅包含数字: " + matcher.matches());

		// 仅包含字母
		matcher = pattern.matcher("abcdggefl");
		System.out.println("仅包含字母: " + matcher.matches());

		// 包含数字和字母
		matcher = pattern.matcher("12478abdefmn");
		System.out.println("包含数字和字母: " + matcher.matches());

		// 仅包含特殊字符
		matcher = pattern.matcher("!@#$%^&%$");
		System.out.println("仅包含特殊字符: " + matcher.matches());

		// 包含数字、字母和特殊字符
		matcher = pattern.matcher("1278abqrxyz!@#$%?");
		System.out.println("包含数字、字母和特殊字符: " + matcher.matches());

		// 长度过短
		matcher = pattern.matcher("1234567");
		System.out.println("长度过短: " + matcher.matches());

		// 长度过长
		matcher = pattern.matcher("1234567890123456789012345678901234567890");
		System.out.println("长度过长: " + matcher.matches());

		// 空字符串
		matcher = pattern.matcher("");
		System.out.println("空字符串: " + matcher.matches());


	}

	@Test
	public void test07() throws Exception {
		Object obj = null;
		String str = (String) obj;
		System.out.println(str);
	}

	@Test
	public void test08() throws Exception {
		int a = -5;
		int b = 3;
		System.out.println("a的补码: " + Integer.toBinaryString(a) + ", length: " + Integer.toBinaryString(a).length());
		System.out.println("b的补码: " + Integer.toBinaryString(b) + ", length: " + Integer.toBinaryString(b).length());
		System.out.println("a&b的补码: " + Integer.toBinaryString(a & b) + ", length: " + Integer.toBinaryString(a & b).length());
		System.out.println("a|b的补码: " + Integer.toBinaryString(a | b) + ", length: " + Integer.toBinaryString(a | b).length());
		System.out.println("a^b的补码: " + Integer.toBinaryString(a ^ b) + ", length: " + Integer.toBinaryString(a ^ b).length());
		System.out.println("~a的补码: " + Integer.toBinaryString(~a) + ", length: " + Integer.toBinaryString(~a).length());
		System.out.println("a<<2的补码: " + Integer.toBinaryString(a << 2) + ", length: " + Integer.toBinaryString(a << 2).length());
		System.out.println("a>>2的补码: " + Integer.toBinaryString(a >> 2) + ", length: " + Integer.toBinaryString(a >> 2).length());
		System.out.println("a>>>2的补码: " + Integer.toBinaryString(a >>> 2) + ", length: " + Integer.toBinaryString(a >>> 2).length());
	}

	@Test
	public void test09() throws Exception {
		BusinessException businessException = new BusinessException(ReturnCode.RC500);
		CompletionException completionException = new CompletionException(businessException);
		// completionException.printStackTrace();
		System.out.println(completionException);
	}

	@Test
	public void test10() throws Exception {
		Exception e = new BusinessException(ReturnCode.RC500);
		if (e instanceof BusinessException) {
			System.out.println("BusinessException");
			System.out.println(e);
			System.out.println((BusinessException) e);
		} else {
			System.out.println("Not BusinessException");
		}
	}

	@Test
	public void test11() throws Exception {
		System.out.println(PathUtil.getExternalPath("./uploads/"));
	}

	@Test
	public void test12() throws Exception {

		MediaType expected = MediaType.parseMediaType("image/*");

		MediaType actual = MediaType.parseMediaType("image/png");

		System.out.println(expected);
		System.out.println(actual);

		System.out.println(expected.isCompatibleWith(actual));
		System.out.println(actual.isCompatibleWith(expected));

		System.out.println(expected.includes(actual));
		System.out.println(actual.includes(expected));

		System.out.println(expected.isWildcardType());
		System.out.println(expected.isWildcardSubtype());

		System.out.println(actual.isWildcardType());
		System.out.println(actual.isWildcardSubtype());
	}

	@Test
	public void test13() throws Exception {
		System.out.println(Runtime.getRuntime().availableProcessors());
		System.out.println(java.lang.Runtime.getRuntime().availableProcessors());
		System.out.println(System.getProperty("java.runtime.availableProcessors"));
	}

	@Test
	public void test14() throws Exception {
		String message = String.format("无效的动作类型: %s，只能使用以下动作类型: %s", UserActionEvent.ActionType.SUPPORT, UserActionEvent.ActionType.ACTION_TYPES);
		System.out.println(message);
	}

	@Test
	public void test15() throws Exception {
		// 基础热度计算
		int views = 0;
		int supp = 100;
		int opp = 0;
		int comm = 0;
		int fav = 0;
		long publishTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 1;
		double base = Math.log(views + 1) * 0.2
				+ (supp + opp) * 0.25
				+ comm * 0.35
				+ fav * 0.2;

		// 时间衰减计算
		long deltaHours = (System.currentTimeMillis() - publishTime) / (3600_000);
		double delta = 0;
		if (deltaHours < 1)
			delta = 0;
		else if (deltaHours < 24)
			delta = 0.05;
		else if (deltaHours < 24 * 3)
			delta = 0.1;
		else if (deltaHours < 24 * 5)
			delta = 0.15;
		else
			delta = 0.2;

		double decay = base * Math.exp(-delta * deltaHours); // λ=0.1

		System.out.println("-delta * deltaHours: " + (-delta * deltaHours));
		System.out.println("delta: " + delta);
		System.out.println("基础热度: " + base);
		System.out.println("时间衰减: " + String.format("%.6f", decay));
	}


	@Test
	public void test16() throws Exception {
		IntStream.range(1, 23).forEach(System.out::println);
	}

	@Test
	public void test17() throws Exception {
		// 创建两个 LocalDateTime 对象
		LocalDateTime dateTime1 = LocalDateTime.of(2025, 4, 2, 14, 59, 59);
		// LocalDateTime dateTime2 = LocalDateTime.of(2023, 10, 5, 12, 0);

		// 计算天数差值
		long daysBetween = ChronoUnit.DAYS.between(dateTime1, LocalDateTime.now());

		System.out.println("天数差值: " + daysBetween); // 输出: 天数差值: 4

	}

	@Test
	public void test18() throws Exception {
		Executors.newSingleThreadExecutor().execute(() -> {
			System.out.println(Thread.currentThread().getId());
			System.out.println(Thread.currentThread().getName());
		});
		System.out.println(Thread.currentThread().getId());
		System.out.println(Thread.currentThread().getName());
	}

	@Test
	public void test19() throws Exception {
		Set<Object> set = Set.of(1L, 2L, 3L);
		System.out.println(set.contains(1L));
		System.out.println(set.contains(1));
		System.out.println(set.contains(4L));
	}

	@Test
	public void test20() throws Exception {
		Class<Long> clazz = Long.class;
		Integer a = 1;
		System.out.println(clazz.cast(a));
	}

	@Test
	public void test21() throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("minAndMaxId", new NewsService.MinAndMaxId(1L, 100L));
		System.out.println(map.get("minAndMaxId"));
		System.out.println(map.get("minAndMaxId").getClass());
		NewsService.MinAndMaxId minAndMaxId = (NewsService.MinAndMaxId) map.get("minAndMaxId");
		System.out.println(minAndMaxId.getMinId());
		System.out.println(minAndMaxId.getMaxId());
	}

	@Test
	public void test22() throws Exception {
		Set<Long> set = new HashSet<>();
		set.add(1L);
		set.add(1L);
		set.add(1L);
		System.out.println(set.size());
	}
}
