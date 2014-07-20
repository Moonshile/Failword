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
import it.sauronsoftware.base64.Base64;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

	/********************************** Methods  ********************************************/

	/**
	 * convert key from string to 256-bits (32-bytes) bytes
	 */
	public static byte[] getKeyBytes(String key) throws NoSuchAlgorithmException{
		MessageDigest md5 = MessageDigest.getInstance("md5");
		byte[] k1 = md5.digest(key.substring(0, key.length()/2).getBytes());
    	byte[] k2 = md5.digest(key.substring(key.length()/2).getBytes());
    	byte[] k = new byte[32];
    	for(int i = 0; i < 16; i++){
    		k[i] = k1[i];
    		k[16 + i] = k2[i];
    	}
    	return k;
	}
	
	/**
	 * encrypt
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchProviderException 
	 */
	public static String encrypt(String plain, byte[] key) 
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
			IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		byte[] p = plain.getBytes();
		byte[] c = crypt(p, key, Cipher.ENCRYPT_MODE);
		return new String(Base64.encode(c));
	}
	
	/**
	 * decrypt
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws NoSuchProviderException 
	 */
	public static String decrypt(String cipher, byte[] key) 
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, 
			IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		byte[] c = Base64.decode(cipher.getBytes());
		byte[] p = crypt(c, key, Cipher.DECRYPT_MODE);
		return new String(p);
	}
	
	
	@SuppressLint("TrulyRandom")
	public static byte[] crypt(byte[] text, byte[] key, int mode) 
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, 
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
		sr.setSeed(key);
		kgen.init(256, sr);
		SecretKey sk = kgen.generateKey();
		SecretKeySpec k = new SecretKeySpec(sk.getEncoded(), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(mode, k);
 		return cipher.doFinal(text);
	}
	
	/********************************** Fields ********************************************/

}
