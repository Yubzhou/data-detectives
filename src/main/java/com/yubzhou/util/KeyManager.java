package com.yubzhou.util;

import com.yubzhou.properties.KeyManagerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serial;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

// 密钥管理类
@Component
public class KeyManager {
	private static final String KEYSTORE_TYPE = "JCEKS";
	private static final String KEY_ALIAS = "myapp_aes_key";
	// System.getProperty("user.home") 其实就是用户目录，比如在 Windows 下就是 C:\Users\用户名
	private final String KEYSTORE_PATH;
	private final char[] KEYSTORE_PASSWORD;

	@Autowired
	public KeyManager(KeyManagerProperties keyManagerProperties) {
		KEYSTORE_PATH = keyManagerProperties.getPath();
		KEYSTORE_PASSWORD = keyManagerProperties.getPassword().toCharArray();
	}

	public SecretKey loadOrCreateKey(String algorithm) {
		File keystoreFile = new File(KEYSTORE_PATH);
		KeyStore ks;

		try {
			ks = KeyStore.getInstance(KEYSTORE_TYPE);

			if (keystoreFile.exists()) {
				loadExistingKeyStore(ks, keystoreFile);
			} else {
				createNewKeyStore(ks, keystoreFile, algorithm);
			}

			return getKeyFromStore(ks);
		} catch (Exception e) {
			throw new KeyManagerException("Key management operation failed", e);
		}
	}

	private void loadExistingKeyStore(KeyStore ks, File keystoreFile) throws Exception {
		try (FileInputStream fis = new FileInputStream(keystoreFile)) {
			ks.load(fis, KEYSTORE_PASSWORD);
		}
	}

	private void createNewKeyStore(KeyStore ks, File keystoreFile, String algorithm) throws Exception {
		ks.load(null, null);
		SecretKey newKey = generateNewKey(algorithm);
		saveKeyToStore(ks, newKey);
		saveKeyStoreToFile(ks, keystoreFile);
	}

	private SecretKey generateNewKey(String algorithm) {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
			keyGen.init(256);
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			throw new KeyManagerException("Key generation failed", e);
		}
	}

	private void saveKeyToStore(KeyStore ks, SecretKey key) throws Exception {
		KeyStore.SecretKeyEntry keyEntry = new KeyStore.SecretKeyEntry(key);
		KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(KEYSTORE_PASSWORD);
		ks.setEntry(KEY_ALIAS, keyEntry, keyPassword);
	}

	private void saveKeyStoreToFile(KeyStore ks, File keystoreFile) throws Exception {
		keystoreFile.getParentFile().mkdirs();
		try (FileOutputStream fos = new FileOutputStream(keystoreFile)) {
			ks.store(fos, KEYSTORE_PASSWORD);
		}
	}

	private SecretKey getKeyFromStore(KeyStore ks) throws Exception {
		if (!ks.containsAlias(KEY_ALIAS)) {
			throw new KeyManagerException("Key alias not found in keystore", null);
		}
		return (SecretKey) ks.getKey(KEY_ALIAS, KEYSTORE_PASSWORD);
	}

	public static class KeyManagerException extends RuntimeException {
		@Serial
		private static final long serialVersionUID = -3361530198974399534L;

		public KeyManagerException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}