/**
 * @Title AESHelper.java
 * @Package com.moonshile.helper
 * @author Duan Kaiqiang (段凯强)
 * @date 2014-7-18
 * @update 
 *
 */
package com.moonshile.helper;

import android.annotation.SuppressLint;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * @author duankq
 *
 */
public class AESHelper {

	/********************************** Constructor ********************************************/

	/********************************** Methods ********************************************/

	/**
	 * encrypt
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public static String encrypt(String plain, byte[] key) 
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
			IllegalBlockSizeException, BadPaddingException{
		return crypt(plain, key, Cipher.ENCRYPT_MODE);
	}
	
	/**
	 * decrypt
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public static String decrypt(String cipher, byte[] key) 
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, 
			IllegalBlockSizeException, BadPaddingException{
		return crypt(cipher, key, Cipher.DECRYPT_MODE);
	}
	
	
	@SuppressLint("TrulyRandom")
	private static String crypt(String text, byte[] key, int mode) 
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
			IllegalBlockSizeException, BadPaddingException{
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(256, new SecureRandom(key));
		SecretKey sk = kgen.generateKey();
		SecretKeySpec k = new SecretKeySpec(sk.getEncoded(), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(mode, k);
		return new String(cipher.doFinal(text.getBytes()));
	}
	
	/********************************** Fields ********************************************/

}
