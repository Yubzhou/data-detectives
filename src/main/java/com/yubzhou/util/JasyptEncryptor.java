package com.yubzhou.util;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor ;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

public class JasyptEncryptor {
	// 环境变量
	private static final String JASYPT_ENCRYPTOR_PASSWORD;

	static {
		JASYPT_ENCRYPTOR_PASSWORD = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
	}

	private JasyptEncryptor() {
	}

	public static String getJasyptEncryptorPassword() {
		return JASYPT_ENCRYPTOR_PASSWORD;
	}

	// 生成加密后的密码
	public static String encrypt(String plainText) {
		PooledPBEStringEncryptor  encryptor = new PooledPBEStringEncryptor ();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();

		// 配置加密参数
		config.setPassword(JASYPT_ENCRYPTOR_PASSWORD); // 设置加密密钥
		config.setAlgorithm("PBEWithHMACSHA512AndAES_256"); // 设置加密算法
		config.setKeyObtentionIterations("1000"); // 密钥生成的迭代次数，影响破解难度
		config.setPoolSize("1"); // 加密池的大小（如果使用了池化加密器）
		config.setProviderName("SunJCE"); // 加密提供者的名称（如 JCE 提供者）
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator"); // 盐生成器的类名，用于防止彩虹表攻击
		config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator"); // 初始向量生成器的类名
		config.setStringOutputType("base64"); // 设置加密字符串的输出类型

		encryptor.setConfig(config);

		// 加密字符串
		return encryptor.encrypt(plainText);
	}

	// 解密密码
	public static String decrypt(String encryptedText) {
		PooledPBEStringEncryptor  decryptor = new PooledPBEStringEncryptor ();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();

		// 配置加密参数
		config.setPassword(JASYPT_ENCRYPTOR_PASSWORD); // 设置加密密钥
		config.setAlgorithm("PBEWithHMACSHA512AndAES_256"); // 设置加密算法
		config.setKeyObtentionIterations("1000"); // 密钥生成的迭代次数，影响破解难度
		config.setPoolSize("1"); // 加密池的大小（如果使用了池化加密器）
		config.setProviderName("SunJCE"); // 加密提供者的名称（如 JCE 提供者）
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator"); // 盐生成器的类名，用于防止彩虹表攻击
		config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator"); // 初始向量生成器的类名
		config.setStringOutputType("base64"); // 设置加密字符串的输出类型

		decryptor.setConfig(config);

		// 解密字符串
		return decryptor.decrypt(encryptedText);
	}

	// public static void main(String[] args) {
	// 	String plainText = "zyb0419man.,@";
	// 	String encryptedText = encrypt(plainText);
	// 	System.out.println("加密后的密码: " + encryptedText);
	//
	// 	String decryptedText = decrypt(encryptedText);
	// 	System.out.println("解密后的密码: " + decryptedText);
	// }
}