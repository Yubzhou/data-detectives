package com.yubzhou;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yubzhou.common.UserRole;
import com.yubzhou.common.UserToken;
import com.yubzhou.util.AESUtil;
import com.yubzhou.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
@Slf4j
public class JwtTest {
	private final JwtUtil jwtUtil;
	private final AESUtil aesUtil;


	@Autowired
	public JwtTest(JwtUtil jwtUtil, AESUtil aesUtil) {
		this.jwtUtil = jwtUtil;
		this.aesUtil = aesUtil;
	}

	@Test
	public void testGenerateToken() {
		String fingerprint = UserToken.generateFingerprint("127.0.0.1", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
				"AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 Edg/133.0.0.0");
		UserToken userToken = new UserToken(1L, UserRole.ADMIN, fingerprint);
		String accessToken = jwtUtil.generateAccessToken(userToken);
		String encryptedRefreshToken = jwtUtil.generateEncryptedRefreshToken(userToken);
		DecodedJWT decodedJWT = jwtUtil.verifyEncryptedRefreshToken(encryptedRefreshToken);// 如果refreshToken失效，会抛出异常
		String rawRefreshToken = aesUtil.decrypt(encryptedRefreshToken);
		System.out.println("accessToken: " + accessToken);
		System.out.println("encryptedRefreshToken: " + encryptedRefreshToken);
		System.out.println("rawRefreshToken: " + rawRefreshToken);
		System.out.println("decodedJWT.getToken(): " + decodedJWT.getToken());
		System.out.println("accessToken claims: " + jwtUtil.getClaims(accessToken));
		System.out.println("refreshToken claims: " + jwtUtil.getClaims(rawRefreshToken));
	}

	@Test
	public void testVerifyToken() {
		String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJhZG1pbiIsInVzZXIiOnsicm9sZSI6ImFkbWluIiwidXNlcm5hbWUiOiJhZG1pbiJ9LCJpYXQiOjE3Mzk0NTU4MjEsImV4cCI6MTczOTQ1OTQyMX0.B3KBXaxYIysaHD-425ZDpGqGqMHETECF6vM9-EIWGSg";
		try {
			String subject = jwtUtil.getSubject(token);
			System.out.println(subject);
			Claim roleClaim = jwtUtil.getClaim(token, "role");
			System.out.println(roleClaim);
			Claim userClaim = jwtUtil.getClaim(token, "user");
			System.out.println(userClaim.as(UserToken.class));
			Map<String, Claim> claims = jwtUtil.getClaims(token);
			System.out.println(claims);
		} catch (JWTVerificationException e) {
			log.error("JWT verification failed: {}", e.getMessage());
		}
	}
}
