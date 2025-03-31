package com.yubzhou.common;

import com.yubzhou.util.ClientFingerprintUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserToken {
	private Long userId;
	private UserRole role;
	private String fingerprint;

	public static String generateFingerprint(HttpServletRequest request) {
		return ClientFingerprintUtil.generate(request);
	}

	public static String generateFingerprint(String clientIp, String userAgent) {
		return ClientFingerprintUtil.generate(clientIp, userAgent);
	}

	public static UserToken of(long userId, UserRole role, String fingerPrint) {
		return new UserToken(userId, role, fingerPrint);
	}
}
