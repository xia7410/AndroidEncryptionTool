package cn.edu.ustc.software.hanyizhao.encryptiontool.tools;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES加密解密工具
 *
 * @author hanyizhao
 *
 */
public class AESTools {

	private Cipher clipherEncrypt = null;
	private Cipher clipherDecrypt = null;

	/**
	 * 初始化一个加密解密工具
	 *
	 * @param key
	 *            任意长度，不为null
	 */
	public AESTools(byte[] key) {
		if (key.length != 16) {
			int sl = key.length;
			byte[] key1 = new byte[16];

			for (int i = 0; i < 16; i++) {
				key1[i] = key[i % sl];
			}
			key = key1;
		}

		try {
			clipherEncrypt = Cipher.getInstance("AES/ECB/PKCS5Padding");
			clipherEncrypt.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key,
					"AES"));
			clipherDecrypt = Cipher.getInstance("AES/ECB/PKCS5Padding");
			clipherDecrypt.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key,
					"AES"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 加密
	 *
	 * @param src
	 * @return
	 */
	public byte[] Encrypt(byte[] src) {
		try {
			return clipherEncrypt.doFinal(src);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 解密
	 *
	 * @param src
	 * @return
	 */
	public byte[] Decrypt(byte[] src) {
		try {
			return clipherDecrypt.doFinal(src);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

}
