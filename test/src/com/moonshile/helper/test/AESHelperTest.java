package com.moonshile.helper.test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.moonshile.failword.LoadingActivity;
import com.moonshile.helper.AESHelper;

import android.test.ActivityInstrumentationTestCase2;


import javax.crypto.Cipher;

public class AESHelperTest extends ActivityInstrumentationTestCase2<LoadingActivity> {
    private LoadingActivity loadingActivity;
    
    public AESHelperTest() {
        super(LoadingActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        loadingActivity = getActivity();
    }

    public void testPreconditions() {
        assertNotNull("loadingActivity is null", loadingActivity);
    }
    
    public void testCrypt() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
    	byte[][] bs = {{12,12}, {12,13}};
    	String key = "asg";
    	byte[] k = AESHelper.getKeyBytes(key);
    	for(byte[] expect: bs){
    		byte[] actual = AESHelper.crypt(AESHelper.crypt(expect, k, Cipher.ENCRYPT_MODE), k, Cipher.DECRYPT_MODE);
    		for(int i = 0; i < expect.length; i++){
    			assertEquals(expect[i], actual[i]);
    		}
    	}
    }

    public void testDecrypt() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
    	String[] plains = { "asc", "123", "熬饿啊 阿瑟个哦i" };
    	String key = "asg";
    	String key2 = "123";
    	byte[] k = AESHelper.getKeyBytes(key);
    	byte[] k2 = AESHelper.getKeyBytes(key2);
    	for(String expect: plains){
    		String actual = AESHelper.decrypt(AESHelper.encrypt(expect, k), k);
    		//String actual2 = AESHelper.decrypt(AESHelper.encrypt(expect, k), k2);
    		assertEquals(expect, actual);
    		//assertFalse(expect.equals(actual2));
    	}
    }
    
    
}
