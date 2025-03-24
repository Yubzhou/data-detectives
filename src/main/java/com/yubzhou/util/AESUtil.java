package com.yubzhou.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

// AES 工具类
@Component
public class AESUtil {
	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final String ALGORITHM = "AES";
	private static final int IV_SIZE = 16;
	private final SecretKey SECRET_KEY;

	@Autowired
	public AESUtil(KeyManager keyManager) {
		SECRET_KEY = keyManager.loadOrCreateKey(ALGORITHM);
	}

	public String encrypt(String plainText) {
		try {
			byte[] iv = new byte[IV_SIZE];
			SecureRandom.getInstanceStrong().nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, ivSpec);

			byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			byte[] encryptedData = new byte[iv.length + cipherText.length];
			System.arraycopy(iv, 0, encryptedData, 0, iv.length);
			System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

			return Base64.getEncoder().encodeToString(encryptedData);
		} catch (Exception e) {
			throw new CryptoException("Encryption failed", e);
		}
	}

	public String decrypt(String encryptedText) {
		try {
			byte[] encryptedData = Base64.getDecoder().decode(encryptedText);
			byte[] iv = Arrays.copyOfRange(encryptedData, 0, IV_SIZE);
			byte[] cipherText = Arrays.copyOfRange(encryptedData, IV_SIZE, encryptedData.length);

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, new IvParameterSpec(iv));

			byte[] decryptedText = cipher.doFinal(cipherText);
			return new String(decryptedText, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new CryptoException("Decryption failed", e);
		}
	}

	public static class CryptoException extends RuntimeException {
		@Serial
		private static final long serialVersionUID = -7310197385013420445L;

		public CryptoException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}