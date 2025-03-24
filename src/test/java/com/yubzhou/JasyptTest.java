package com.yubzhou;

import com.yubzhou.util.JasyptEncryptor;
import org.junit.jupiter.api.Test;

public class JasyptTest {

	@Test
	public void testGetJasyptEncryptorPassword() {
		// 获取到Windows环境变量里面的值
		System.out.println(System.getenv("JASYPT_ENCRYPTOR_PASSWORD"));
	}

	// 使用Jasypt来加密密码
	@Test
	public void testEncryptPassword() {
		// 要加密的明文密码
		String plainTextPassword = "123456";
		System.out.println("密钥：" + JasyptEncryptor.getJasyptEncryptorPassword());
		// 生成加密后的密码
		String encryptedPassword = JasyptEncryptor.encrypt(plainTextPassword);
		System.out.println("加密后的密码：" + encryptedPassword);
		// 解密密码
		String decryptedPassword = JasyptEncryptor.decrypt(encryptedPassword);
		System.out.println("解密后的密码：" + decryptedPassword);
	}

	// 使用Jasypt来解密密码
	@Test
	public void testDecryptPassword() {
		// 要解密的密文密码
		String encryptedPassword = "123456";	// 密文密码
		System.out.println("密钥：" + JasyptEncryptor.getJasyptEncryptorPassword());
		// 解密密码
		String decryptedPassword = JasyptEncryptor.decrypt(encryptedPassword);
		System.out.println("解密后的密码：" + decryptedPassword);
	}
}
