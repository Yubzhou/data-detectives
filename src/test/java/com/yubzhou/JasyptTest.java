package com.yubzhou;

import com.yubzhou.util.JasyptEncryptor;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JasyptTest {

	private void batchEncryptPassword(List<String> plainTextPasswords) {
		String encryptedPassword, decryptedPassword;
		int i = 1;
		for (String plainTextPassword : plainTextPasswords) {
			System.out.println("第" + i++ + "个密码：");
			System.out.println("\t明文密码：" + plainTextPassword);
			encryptedPassword = JasyptEncryptor.encrypt(plainTextPassword);
			System.out.println("\t加密后的密码：" + encryptedPassword);
			decryptedPassword = JasyptEncryptor.decrypt(encryptedPassword);
			System.out.println("\t解密后的密码：" + decryptedPassword);
			System.out.println("\n");
		}
	}

	private void batchDecryptPassword(List<String> encryptedTextPasswords) {
		String decryptedPassword;
		int i = 1;
		for (String encryptedTextPassword : encryptedTextPasswords) {
			System.out.println("第" + i++ + "个密码：");
			System.out.println("\t密文密码：" + encryptedTextPassword);
			decryptedPassword = JasyptEncryptor.decrypt(encryptedTextPassword);
			System.out.println("\t解密后的密码：" + decryptedPassword);
			System.out.println("\n");
		}
	}

	@Test
	public void testGetJasyptEncryptorPassword() {
		// 获取到Windows环境变量里面的值
		System.out.println(System.getenv("JASYPT_ENCRYPTOR_PASSWORD"));
	}


	// 使用Jasypt来加密密码
	@Test
	public void testEncryptPassword() {
		List<String> plainTextPasswords = List.of("123456");
		batchEncryptPassword(plainTextPasswords);
	}

	// 使用Jasypt来解密密码
	@Test
	public void testDecryptPassword() {
		List<String> encryptedTextPasswords = List.of("123456");
		batchDecryptPassword(encryptedTextPasswords);
	}
}
