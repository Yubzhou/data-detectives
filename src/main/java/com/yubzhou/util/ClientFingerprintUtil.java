package com.yubzhou.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class ClientFingerprintUtil {

	// 定义一个字符数组，存储16进制字符
	private final static char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

	public static String generate(HttpServletRequest request) {
		String ip = getClientIp(request);
		String userAgent = request.getHeader("User-Agent");
		// String deviceId = request.getHeader("X-Device-ID");

		log.info("ClientFingerprintUtil中：【ip: {}】,【userAgent: {}】", ip, userAgent);

		String rawData = buildRawData(ip, userAgent);
		return sha256(rawData);
	}

	public static String generate(String clientIp, String userAgent) {
		String rawData = buildRawData(clientIp, userAgent);
		return sha256(rawData);
	}

	public static String getClientIp(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-Forwarded-For");
		if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
		}
		// 处理多级代理的情况，取第一个非unknown的IP
		if (ipAddress != null && ipAddress.contains(",")) {
			ipAddress = ipAddress.split(",")[0];
		}
		// 如果是本地环境，则使用127.0.0.1代替
		if ("0:0:0:0:0:0:0:1".equals(ipAddress)) ipAddress = "127.0.0.1";
		return ipAddress;
	}

	private static String buildRawData(String ip, String ua) {
		return String.format("%s|%s",
				maskIp(ip),
				ua != null ? ua : ""
		);
	}

	// 动态IP处理：对IPv4保留前3段（如192.168.1.x）
	private static String maskIp(String ip) {
		if (ip == null || ip.trim().isEmpty()) return "";
		String[] segments = ip.split("\\.");
		if (segments.length == 4) {
			return segments[0] + "." + segments[1] + "." + segments[2] + ".x";
		}
		return ip;
	}

	private static String sha256(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(input.getBytes());
			return bytesToHexString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256不可用", e);
		}
	}

	// private static String bytesToHex(byte[] bytes) {
	// 	StringBuilder sb = new StringBuilder();
	// 	for (byte b : bytes) {
	// 		sb.append(String.format("%02x", b));
	// 	}
	// 	return sb.toString();
	// }

	public static String bytesToHexString(byte[] bytes) {
		// 每个字节转换为两个16进制字符，所以结果长度是字节数组长度的两倍
		char[] hexChars = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			// 将字节转换为无符号整数
			int unsignedByte = bytes[i] & 0xFF;
			// 高4位的16进制字符
			hexChars[i * 2] = HEX_DIGITS[unsignedByte >>> 4];
			// 低4位的16进制字符
			hexChars[i * 2 + 1] = HEX_DIGITS[unsignedByte & 0x0F];
		}
		return new String(hexChars);
	}

	// public static void main(String[] args) {
	// 	String s = "第v南德斯u你好吗";
	// 	byte[] bytes = s.getBytes();
	// 	String hex1 = bytesToHex(bytes);
	// 	String hex2 = bytesToHexString(bytes);
	// 	System.out.println(hex1);
	// 	System.out.println(hex2);
	// 	System.out.println(hex1.equalsIgnoreCase(hex2));
	// }

	// public static void main(String[] args) {
	// 	String ip = "192.168.1.100";
	// 	String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
	// 	String rawData = buildRawData(ip, userAgent);
	// 	String fingerprint = sha256(rawData);
	// 	System.out.println("rawData: " + rawData);
	// 	System.out.println("fingerprint: " + fingerprint);
	// }
}