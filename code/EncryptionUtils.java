package org.util.algorithm;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * <b>密码工具类</b>
 * <p>
 * 提供可逆算法和不可逆算法
 * </p>
 * 
 * @author wuzhipeng
 */
public class EncryptionUtils {
	// org.slf4j.Logger;
	// protected final static Logger logger = LoggerFactory.getLogger(EncryptionUtils.class);
	protected final static Logger logger = Logger.getLogger(EncryptionUtils.class);
	protected final static String CHARSET_UTF8 = "UTF-8";

	/**
	 * 摘要加密
	 * 
	 * @param algorithm
	 *            算法名称
	 * @param plaintext
	 *            明文
	 * @return
	 */
	private static byte[] encryptByDigest(String algorithm, byte[] plaintext) {
		try {
			// 获得摘要算法的 MessageDigest 对象
			MessageDigest messageDigest = java.security.MessageDigest.getInstance(algorithm);

			// 使用明文字节码更新摘要
			messageDigest.update(plaintext);
			// 获得密文字节码
			byte[] ciphertext = messageDigest.digest();

			return ciphertext;
		} catch (NoSuchAlgorithmException e) {
			String msg = String.format("算法[%s]设置错误，加密失败。", algorithm);
			logger.error(msg, e);
		}

		return null;
	}

	/**
	 * 摘要加密
	 * 
	 * @param algorithm
	 *            算法名称
	 * @param plaintext
	 *            明文
	 * @return
	 */
	private static String encryptByDigest(String algorithm, String plaintext) {
		try {
			byte[] bytePlaintext = plaintext.getBytes(CHARSET_UTF8);
			// 获得密文字节码
			byte[] byteCiphertext = encryptByDigest(algorithm, bytePlaintext);
			// 将字节码转换为十六进制字符串
			String ciphertext = byteArray2HexString(byteCiphertext);

			return ciphertext;
		} catch (UnsupportedEncodingException e) {
			logger.error("加密失败，字符集设置错误", e);
		}

		return null;
	}

	/**
	 * 使用SHA-1算法计算摘要
	 * 
	 * @param plaintext
	 *            明文字节码
	 * @return
	 */
	public static String encryptBySha1(byte[] plaintext) {
		// 获得密文字节码
		byte[] byteCiphertext = encryptByDigest(EnumAlgorithm.SHA1.getValue(), plaintext);
		// 将字节码转换为十六进制字符串
		String ciphertext = byteArray2HexString(byteCiphertext);

		return ciphertext;
	}

	/**
	 * 使用SHA-256算法计算摘要
	 * 
	 * @param plaintext
	 *            明文字节码
	 * @return
	 */
	public static String encryptBySha256(byte[] plaintext) {
		// 获得密文字节码
		byte[] byteCiphertext = encryptByDigest(EnumAlgorithm.SHA256.getValue(), plaintext);
		// 将字节码转换为十六进制字符串
		String ciphertext = byteArray2HexString(byteCiphertext);

		return ciphertext;
	}

	/**
	 * 使用SHA-512算法计算摘要
	 * 
	 * @param plaintext
	 *            明文字节码
	 * @return
	 */
	public static String encryptBySha512(byte[] plaintext) {
		// 获得密文字节码
		byte[] byteCiphertext = encryptByDigest(EnumAlgorithm.SHA512.getValue(), plaintext);
		// 将字节码转换为十六进制字符串
		String ciphertext = byteArray2HexString(byteCiphertext);

		return ciphertext;
	}

	/**
	 * 使用MD5算法计算摘要
	 * 
	 * @param plaintext
	 *            明文字节码
	 * @return
	 */
	public static String encryptByMd5(byte[] plaintext) {
		// 获得密文字节码
		byte[] byteCiphertext = encryptByDigest(EnumAlgorithm.MD5.getValue(), plaintext);
		// 将字节码转换为十六进制字符串
		String ciphertext = byteArray2HexString(byteCiphertext);

		return ciphertext;
	}

	/**
	 * 使用SHA-1算法计算摘要
	 * 
	 * @param plaintext
	 *            明文
	 * @return
	 */
	public static String encryptBySha1(String plaintext) {
		String ciphertext = encryptByDigest(EnumAlgorithm.SHA1.getValue(), plaintext);

		return ciphertext;
	}

	/**
	 * 使用SHA-256算法计算摘要
	 * 
	 * @param plaintext
	 *            明文
	 * @return
	 */
	public static String encryptBySha256(String plaintext) {
		String ciphertext = encryptByDigest(EnumAlgorithm.SHA256.getValue(), plaintext);

		return ciphertext;
	}

	/**
	 * 使用SHA-512算法计算摘要
	 * 
	 * @param plaintext
	 *            明文
	 * @return
	 */
	public static String encryptBySha512(String plaintext) {
		String ciphertext = encryptByDigest(EnumAlgorithm.SHA512.getValue(), plaintext);

		return ciphertext;
	}

	/**
	 * 使用MD5算法计算摘要
	 * 
	 * @param plaintext
	 *            明文
	 * @return
	 */
	public static String encryptByMd5(String plaintext) {
		String ciphertext = encryptByDigest(EnumAlgorithm.MD5.getValue(), plaintext);

		return ciphertext;
	}

	/**
	 * 加密数据（AES算法ECB模式零补码）
	 * 
	 * @param plaintext
	 *            待加密明文
	 * @param key
	 *            加密密钥
	 * @return 密文
	 */
	public static byte[] encryptByAesEcbNoPadding(byte[] plaintext, byte[] key) {
		if (null == plaintext || plaintext.length == 0) {
			return null;
		}
		if (null == key || key.length == 0) {
			return null;
		}

		// 算法
		final String algorithm = EnumAlgorithm.AES.getValue();
		// 模式
		final String pattern = "ECB";
		// 补码方式
		final String padding = "NoPadding";

		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, algorithm);
			// "算法/模式/补码方式"
			String transformation = String.format("%s/%s/%s", algorithm, pattern, padding);
			Cipher cipher = Cipher.getInstance(transformation);
			int blockSize = cipher.getBlockSize();

			int plaintextLength = plaintext.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}

			byte[] bytePlaintext = new byte[plaintextLength];
			System.arraycopy(plaintext, 0, bytePlaintext, 0, plaintext.length);

			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] ciphertext = cipher.doFinal(bytePlaintext);

			return ciphertext;
		} catch (NoSuchAlgorithmException e) {
			String msg = String.format("算法[%s]设置异常，加密失败。", algorithm);
			logger.error(msg, e);
		} catch (NoSuchPaddingException e) {
			String msg = String.format("补码方式[%s]设置异常，加密失败。", padding);
			logger.error(msg, e);
		} catch (InvalidKeyException e) {
			String msg = "密钥设置异常，加密失败。";
			logger.error(msg, e);
		} catch (IllegalBlockSizeException e) {
			String msg = "数据块大小设置异常，加密失败。";
			logger.error(msg, e);
		} catch (BadPaddingException e) {
			String msg = "数据补码异常，加密失败。";
			logger.error(msg, e);
		}

		return null;
	}

	/**
	 * 解密数据（AES算法ECB模式零补码）
	 * 
	 * @param ciphertext
	 *            待解密密文
	 * @param key
	 *            解密密钥
	 * @return 明文
	 */
	public static byte[] decryptByAesEcbNoPadding(byte[] ciphertext, byte[] key) {
		if (null == ciphertext || ciphertext.length == 0) {
			return null;
		}
		if (null == key || key.length == 0) {
			return null;
		}

		// 算法
		final String algorithm = EnumAlgorithm.AES.getValue();
		// 模式
		final String pattern = "ECB";
		// 补码方式
		final String padding = "NoPadding";

		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, algorithm);
			// "算法/模式/补码方式"
			String transformation = String.format("%s/%s/%s", algorithm, pattern, padding);
			Cipher cipher = Cipher.getInstance(transformation);

			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] plaintext = cipher.doFinal(ciphertext);

			return plaintext;
		} catch (NoSuchAlgorithmException e) {
			String msg = String.format("算法[%s]设置异常，解密失败。", algorithm);
			logger.error(msg, e);
		} catch (NoSuchPaddingException e) {
			String msg = String.format("补码方式[%s]设置异常，解密失败。", padding);
			logger.error(msg, e);
		} catch (InvalidKeyException e) {
			String msg = "密钥设置异常，解密失败。";
			logger.error(msg, e);
		} catch (IllegalBlockSizeException e) {
			String msg = "数据块大小设置异常，解密失败。";
			logger.error(msg, e);
		} catch (BadPaddingException e) {
			String msg = "数据补码异常，解密失败。";
			logger.error(msg, e);
		}

		return null;
	}

	/**
	 * 加密数据（AES算法ECB模式零补码）
	 * 
	 * @param plaintext
	 *            待加密明文（UTF-8字符集字符串）
	 * @param key
	 *            加密密钥（十六进制字符串）
	 * @return 密文（UTF-8字符集Base64编码字符串）
	 */
	public static String encryptByAesEcbNoPadding(String plaintext, String key) {
		if (StringUtils.isBlank(plaintext) || StringUtils.isBlank(key)) {
			return null;
		}

		String ciphertext = null;

		try {
			byte[] byteContent = plaintext.getBytes(CHARSET_UTF8);
			byte[] byteKey = hexString2ByteArray(key);

			byte[] byteCiphertext = encryptByAesEcbNoPadding(byteContent, byteKey);

			if (null != byteCiphertext) {
				// ciphertext = byteArray2HexString(byteCiphertext);
				ciphertext = byteArray2Base64String(byteCiphertext);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("加密失败，字符集设置错误", e);
		}

		return ciphertext;
	}

	/**
	 * 解密数据（AES算法ECB模式零补码）
	 * 
	 * @param ciphertext
	 *            待解密密文（UTF-8字符集Base64编码字符串）
	 * @param key
	 *            解密密钥（十六进制字符串）
	 * @return 明文（UTF-8字符集字符串）
	 */
	public static String decryptByAesEcbNoPadding(String ciphertext, String key) {
		if (StringUtils.isBlank(ciphertext) || StringUtils.isBlank(key)) {
			return null;
		}

		String plaintext = null;

		// byte[] byteContent = hexString2ByteArray(ciphertext);
		byte[] byteContent = base64String2byteArray(ciphertext);
		byte[] byteKey = hexString2ByteArray(key);

		byte[] bytePlaintext = decryptByAesEcbNoPadding(byteContent, byteKey);

		if (null != bytePlaintext) {
			try {
				plaintext = new String(bytePlaintext, CHARSET_UTF8);
				plaintext = plaintext.trim();
			} catch (UnsupportedEncodingException e) {
				logger.error("解密失败，字符集设置错误", e);
			}
		}

		return plaintext;
	}

	/**
	 * 加密数据（AES算法CBC模式零补码）
	 * 
	 * @param plaintext
	 *            待加密明文
	 * @param key
	 *            加密密钥
	 * @param iv
	 *            加密向量
	 * @return 密文
	 */
	public static byte[] encryptByAesCbcNoPadding(byte[] plaintext, byte[] key, byte[] iv) {
		if (null == plaintext || plaintext.length == 0) {
			return null;
		}
		if (null == key || key.length == 0) {
			return null;
		}
		if (null == iv || iv.length == 0) {
			return null;
		}

		// 算法
		final String algorithm = EnumAlgorithm.AES.getValue();
		// 模式
		final String pattern = "CBC";
		// 补码方式
		final String padding = "NoPadding";

		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, algorithm);
			// "算法/模式/补码方式"
			String transformation = String.format("%s/%s/%s", algorithm, pattern, padding);
			Cipher cipher = Cipher.getInstance(transformation);
			int blockSize = cipher.getBlockSize();

			int plaintextLength = plaintext.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}

			byte[] bytePlaintext = new byte[plaintextLength];
			System.arraycopy(plaintext, 0, bytePlaintext, 0, plaintext.length);

			// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
			IvParameterSpec ivps = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivps);
			byte[] ciphertext = cipher.doFinal(bytePlaintext);

			return ciphertext;
		} catch (NoSuchAlgorithmException e) {
			String msg = String.format("算法[%s]设置异常，加密失败。", algorithm);
			logger.error(msg, e);
		} catch (NoSuchPaddingException e) {
			String msg = String.format("补码方式[%s]设置异常，加密失败。", padding);
			logger.error(msg, e);
		} catch (InvalidKeyException e) {
			String msg = "密钥设置异常，加密失败。";
			logger.error(msg, e);
		} catch (InvalidAlgorithmParameterException e) {
			String msg = "算法参数设置异常，加密失败。";
			logger.error(msg, e);
		} catch (IllegalBlockSizeException e) {
			String msg = "数据块大小设置异常，加密失败。";
			logger.error(msg, e);
		} catch (BadPaddingException e) {
			String msg = "数据补码异常，加密失败。";
			logger.error(msg, e);
		}

		return null;
	}

	/**
	 * 解密数据（AES算法CBC模式零补码）
	 * 
	 * @param ciphertext
	 *            待解密密文
	 * @param key
	 *            解密密钥
	 * @param iv
	 *            解密向量
	 * @return 明文
	 */
	public static byte[] decryptByAesCbcNoPadding(byte[] ciphertext, byte[] key, byte[] iv) {
		if (null == ciphertext || ciphertext.length == 0) {
			return null;
		}
		if (null == key || key.length == 0) {
			return null;
		}
		if (null == iv || iv.length == 0) {
			return null;
		}

		// 算法
		final String algorithm = EnumAlgorithm.AES.getValue();
		// 模式
		final String pattern = "CBC";
		// 补码方式
		final String padding = "NoPadding";

		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, algorithm);
			// "算法/模式/补码方式"
			String transformation = String.format("%s/%s/%s", algorithm, pattern, padding);
			Cipher cipher = Cipher.getInstance(transformation);

			// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
			IvParameterSpec ivps = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivps);
			byte[] plaintext = cipher.doFinal(ciphertext);

			return plaintext;
		} catch (NoSuchAlgorithmException e) {
			String msg = String.format("算法[%s]设置异常，解密失败。", algorithm);
			logger.error(msg, e);
		} catch (NoSuchPaddingException e) {
			String msg = String.format("补码方式[%s]设置异常，解密失败。", padding);
			logger.error(msg, e);
		} catch (InvalidKeyException e) {
			String msg = "密钥设置异常，解密失败。";
			logger.error(msg, e);
		} catch (InvalidAlgorithmParameterException e) {
			String msg = "算法参数设置异常，解密失败。";
			logger.error(msg, e);
		} catch (IllegalBlockSizeException e) {
			String msg = "数据块大小设置异常，解密失败。";
			logger.error(msg, e);
		} catch (BadPaddingException e) {
			String msg = "数据补码异常，解密失败。";
			logger.error(msg, e);
		}

		return null;
	}

	/**
	 * 加密数据（AES算法CBC模式零补码）
	 * 
	 * @param plaintext
	 *            待加密明文（UTF-8字符集字符串）
	 * @param key
	 *            加密密钥（十六进制字符串）
	 * @param iv
	 *            加密向量（十六进制字符串）
	 * @return 密文（UTF-8字符集Base64编码字符串）
	 */
	public static String encryptByAesCbcNoPadding(String plaintext, String key, String iv) {
		if (StringUtils.isBlank(plaintext) || StringUtils.isBlank(key) || StringUtils.isBlank(iv)) {
			return null;
		}

		String ciphertext = null;

		try {
			byte[] bytePlaintext = plaintext.getBytes(CHARSET_UTF8);
			byte[] byteKey = hexString2ByteArray(key);
			byte[] byteIv = hexString2ByteArray(iv);

			byte[] byteCiphertext = encryptByAesCbcNoPadding(bytePlaintext, byteKey, byteIv);

			if (null != byteCiphertext) {
				// ciphertext = byteArray2HexString(byteCiphertext);
				ciphertext = byteArray2Base64String(byteCiphertext);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("加密失败，字符集设置错误", e);
		}

		return ciphertext;
	}

	/**
	 * 解密数据（AES算法CBC模式零补码）
	 * 
	 * @param ciphertext
	 *            待解密密文（UTF-8字符集Base64编码字符串）
	 * @param key
	 *            解密密钥（十六进制字符串）
	 * @param iv
	 *            解密向量（十六进制字符串）
	 * @return 明文（UTF-8字符集字符串）
	 */
	public static String decryptByAesCbcNoPadding(String ciphertext, String key, String iv) {
		if (StringUtils.isBlank(ciphertext) || StringUtils.isBlank(key) || StringUtils.isBlank(iv)) {
			return null;
		}

		String plaintext = null;

		// byte[] byteCiphertext = hexString2ByteArray(ciphertext);
		byte[] byteCiphertext = base64String2byteArray(ciphertext);
		byte[] byteKey = hexString2ByteArray(key);
		byte[] byteIv = hexString2ByteArray(iv);

		byte[] bytePlaintext = decryptByAesCbcNoPadding(byteCiphertext, byteKey, byteIv);

		if (null != bytePlaintext) {
			try {
				plaintext = new String(bytePlaintext, CHARSET_UTF8);
				plaintext = plaintext.trim();
			} catch (UnsupportedEncodingException e) {
				logger.error("解密失败，字符集设置错误", e);
			}
		}

		return plaintext;
	}

	/**
	 * 将字节码转换为Base64字符串
	 * 
	 * @param binaryData
	 * @return
	 */
	public static String byteArray2Base64String(byte[] binaryData) {
		if (null == binaryData || binaryData.length == 0) {
			return null;
		}

		try {
			// String base64String = org.apache.commons.codec.binary.Base64.encodeBase64String(binaryData);
			byte[] base64Data = org.apache.commons.codec.binary.Base64.encodeBase64(binaryData);
			String base64String = new String(base64Data, CHARSET_UTF8);
			return base64String;
		} catch (UnsupportedEncodingException e) {
			logger.error("转换失败，字符集设置错误", e);
		}

		return null;
	}

	/**
	 * 将Base64字符串转换为字节码
	 * 
	 * @param base64String
	 * @return
	 */
	public static byte[] base64String2byteArray(String base64String) {
		if (StringUtils.isBlank(base64String)) {
			return null;
		}

		try {
			// byte[] binaryData = org.apache.commons.codec.binary.Base64.decodeBase64(base64String);
			byte[] base64Data = base64String.getBytes(CHARSET_UTF8);
			byte[] binaryData = org.apache.commons.codec.binary.Base64.decodeBase64(base64Data);
			return binaryData;
		} catch (UnsupportedEncodingException e) {
			logger.error("转换失败，字符集设置错误", e);
		}

		return null;
	}

	/**
	 * 将字节码转换为十六进制字符串
	 * 
	 * @param bytes
	 *            字节码
	 * @return
	 */
	public static String byteArray2HexString(byte[] binaryData) {
		if (null == binaryData || binaryData.length == 0) {
			return null;
		}

		StringBuffer hex = new StringBuffer();
		for (byte b : binaryData) {
			int v = b & 0xFF;
			String segment = Integer.toHexString(v);
			if (segment.length() < 2) {
				hex.append(0);
			}
			hex.append(segment);
		}

		return hex.toString();
	}

	/**
	 * 将十六进制字符串转换为字节码
	 * 
	 * @param hexString
	 *            十六进制字符串
	 * @return
	 */
	public static byte[] hexString2ByteArray(String hexString) {
		if (StringUtils.isBlank(hexString)) {
			return null;
		}

		int length = hexString.length() / 2;
		byte[] bytes = new byte[length];

		for (int i = 0; i < length; i++) {
			int high = Integer.parseInt(hexString.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexString.substring(i * 2 + 1, i * 2 + 2), 16);

			bytes[i] = (byte) (high * 16 + low);
		}

		return bytes;
	}

	/**
	 * 将十六进制字符串转换为字节码（算法二）
	 * 
	 * @param hexString
	 *            十六进制字符串
	 * @return
	 */
	public static byte[] hexString2ByteArray2(String hexString) {
		if (StringUtils.isBlank(hexString)) {
			return null;
		}

		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;

		char[] hexChars = hexString.toCharArray();
		byte[] bytes = new byte[length];

		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			bytes[i] = (byte) (char2Byte(hexChars[pos]) << 4 | char2Byte(hexChars[pos + 1]));
		}
		return bytes;
	}

	private static byte char2Byte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String createKey(String key) {
		try {
			// 实例化
			KeyGenerator kgen;
			kgen = KeyGenerator.getInstance(EnumAlgorithm.AES.getValue());
			// 设置密钥长度
			kgen.init(128, new SecureRandom(key.getBytes()));
			// 生成密钥
			SecretKey skey = kgen.generateKey();
			// 返回密钥的二进制编码
			byte[] byteKey = skey.getEncoded();

			return byteArray2HexString(byteKey);

		} catch (NoSuchAlgorithmException e) {
			logger.error("密钥生成失败", e);
		}

		return null;
	}

	public static void main(String[] args) {
		// System.out.println(encryptByMd5("wxappid_test"));
		// System.out.println("6d8b61153dd62f718d7c80f3be590cbf");
		// System.out.println(encryptBySha1("wxappid_test"));
		// System.out.println("d374ac57edbf0a40b9b34cf5211d203ef73967c8");
		// System.out.println(encryptByDigest(EnumAlgorithm.SHA256.getValue(), "wxappid_test"));
		// System.out.println("3fdf15b0b169791cdfb4802ae4401d582e45dad30ffc15c73177041cc2278d78");
		// System.out.println(encryptByDigest(EnumAlgorithm.SHA512.getValue(), "wxappid_test"));
		// System.out.println("d894ab05d557f038a9c1d38bea53144ac570023bee3c4d718b833ac2a00e4e70212f840b363e8bb8d077d304730b54708c91f26f5588ffcf214cb40eb7b9d653");
		String key = createKey("加密密钥");
		String iv = createKey("加密向量");
		String msg = "天天好心情";
		String a = encryptByAesEcbNoPadding(msg, key);
		System.out.println(a);
		String b = decryptByAesEcbNoPadding(a, key);
		System.out.println(b);
		String c = encryptByAesCbcNoPadding(msg, key, iv);
		System.out.println(c);
		String d = decryptByAesCbcNoPadding(c, key, iv);
		System.out.println(d);
	}

	/**
	 * 加密算法枚举
	 */
	public enum EnumAlgorithm {
		SHA1("SHA-1"), SHA256("SHA-256"), SHA512("SHA-512"), MD5("MD5"), DES("DES"), AES("AES");

		private final String value;

		private EnumAlgorithm(String value) {
			this.value = value;
		}

		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

	}

}