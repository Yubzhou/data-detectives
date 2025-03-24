package com.yubzhou.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yubzhou.common.ReturnCode;
import com.yubzhou.common.UserToken;
import com.yubzhou.exception.BusinessException;
import com.yubzhou.properties.JwtProperties;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class JwtUtil {
	public static final String USER_CLAIM_KEY = "user"; // 用户声明键

	private final JwtProperties jwtProperties;
	private final AESUtil aesUtil;

	@Autowired
	public JwtUtil(JwtProperties jwtProperties, AESUtil aesUtil) {
		this.jwtProperties = jwtProperties;
		this.aesUtil = aesUtil;
	}

	/**
	 * 生成 AccessToken
	 *
	 * @param userToken 用户相关信息
	 * @return String AccessToken字符串（不带前缀Bearer ）
	 */
	public String generateAccessToken(UserToken userToken) {
		return generateToken(userToken, true);
	}

	/**
	 * 生成 RefreshToken
	 *
	 * @param userToken 用户相关信息
	 * @return String RefreshToken字符串（不带前缀Bearer ）
	 */
	public String generateRefreshToken(UserToken userToken) {
		return generateToken(userToken, false);
	}

	/**
	 * 生成加密的RefreshToken
	 *
	 * @param rawRefreshToken 不带token前缀的RefreshToken字符串
	 * @return String 加密后的RefreshToken字符串
	 */
	public String generateEncryptedRefreshToken(String rawRefreshToken) {
		return aesUtil.encrypt(rawRefreshToken);
	}

	/**
	 * 生成加密的RefreshToken
	 *
	 * @param userToken 用户相关信息
	 * @return String 加密后的RefreshToken字符串
	 */
	public String generateEncryptedRefreshToken(UserToken userToken) {
		String rawRefreshToken = generateRefreshToken(userToken); // 生成不带token前缀的RefreshToken字符串
		return aesUtil.encrypt(rawRefreshToken);
	}

	/**
	 * 验证加密的RefreshToken
	 *
	 * @param encryptedRefreshToken 加密后的RefreshToken字符串
	 * @return DecodedJWT 包含 JWT 所有信息的对象
	 */
	public DecodedJWT verifyEncryptedRefreshToken(String encryptedRefreshToken) {
		String rawRefreshToken;
		try {
			rawRefreshToken = aesUtil.decrypt(encryptedRefreshToken); // 解密RefreshToken字符串
			return verifyToken(rawRefreshToken); // 验证RefreshToken
		} catch (Exception e) {
			throw new BusinessException(ReturnCode.INVALID_TOKEN.getCode(), "刷新token已失效");
		}
	}


	/**
	 * 验证 JWT
	 *
	 * @param token JWT字符串（可带或不带前缀Bearer ）
	 * @return DecodedJWT 包含 JWT 所有信息的对象
	 * @throws JWTVerificationException 验证失败时抛出异常
	 */
	public DecodedJWT verifyToken(String token) throws JWTVerificationException {
		JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtProperties.getSecretKey())).build();
		return verifier.verify(token.replace(jwtProperties.getTokenPrefix(), ""));
	}

	/**
	 * 获取 JWT 中的唯一标识，一般为用户名
	 *
	 * @param token JWT字符串（可带或不带前缀Bearer ）
	 * @return String 唯一标识
	 * @throws JWTVerificationException 验证失败时抛出异常
	 */
	public String getSubject(String token) throws JWTVerificationException {
		return verifyToken(token).getSubject();
	}

	/**
	 * 获取 JWT 中的用户信息，返回 UserToken 对象
	 *
	 * @param token JWT字符串（可带或不带前缀Bearer ）
	 * @return UserToken 用户信息
	 * @throws JWTVerificationException 验证失败时抛出异常
	 */
	public UserToken getUserToken(String token) throws JWTVerificationException {
		Claim userClaim = getClaim(token, USER_CLAIM_KEY); // 获取到用户信息（json字符串）
		return userClaim.as(UserToken.class); // 解析json字符串为UserToken对象
	}

	/**
	 * 获取 JWT 中的全部声明，返回 Map
	 *
	 * @param token：JWT字符串（可带或不带前缀Bearer ）
	 * @return Map<String, Claim> 全部声明
	 * @throws JWTVerificationException 验证失败时抛出异常
	 */
	public Map<String, Claim> getClaims(String token) throws JWTVerificationException {
		return verifyToken(token).getClaims();
	}

	/**
	 * 获取 JWT 中的指定声明，返回 Claim
	 *
	 * @param token：JWT字符串（可带或不带前缀Bearer ）
	 * @param key：声明键
	 * @return Claim 声明值，返回结果为 json 格式的 Claim 对象（即直接输出 Claim 对象时，会自动转为 json 格式）
	 * @throws JWTVerificationException 验证失败时抛出异常
	 */
	public Claim getClaim(String token, String key) throws JWTVerificationException {
		return verifyToken(token).getClaim(key);
	}

	/**
	 * 将UserToken对象转换为Map对象
	 *
	 * @param userToken UserToken对象
	 * @return Map对象
	 */
	private Map<String, Object> userTokenToMap(UserToken userToken) {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", userToken.getUserId());
		map.put("role", userToken.getRole().name()); // 将枚举类型转换为字符串，要不然无法序列化
		map.put("fingerprint", userToken.getFingerprint());
		return map;
	}

	/**
	 * 生成 JWT
	 *
	 * @param userToken 用户信息
	 * @return String JWT字符串（不带前缀Bearer ）
	 */
	private String generateToken(UserToken userToken, boolean isAccessToken) {
		long currentTimeMillis = System.currentTimeMillis(); // 当前时间戳（毫秒）
		return JWT.create()
				.withSubject(String.valueOf(userToken.getUserId())) // 唯一标识
				// withClaim() 方法可以添加自定义声明，键为字符串，值可为基本类型、数组、Map、字符串等
				.withClaim("is_access_token", isAccessToken) // 防止同一时间访问token和刷新token同时生成，而导致token接近
				.withClaim(USER_CLAIM_KEY, userTokenToMap(userToken)) // 添加自定义声明
				.withIssuedAt(new Date(currentTimeMillis)) // 签发时间
				.withExpiresAt(new Date(currentTimeMillis + jwtProperties.getTokenExpirationTime(isAccessToken))) // 过期时间
				.sign(Algorithm.HMAC256(jwtProperties.getSecretKey())); // 签名：使用 HMAC256 算法
	}
}