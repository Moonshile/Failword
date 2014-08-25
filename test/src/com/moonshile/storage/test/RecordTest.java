package com.moonshile.storage.test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.moonshile.failword.LoadingActivity;
import com.moonshile.helper.AESHelper;
import com.moonshile.storage.Record;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;

public class RecordTest extends ActivityInstrumentationTestCase2<LoadingActivity>{

	private LoadingActivity loadingActivity;
	private byte[] key;
	
	
	public RecordTest() {
		super(LoadingActivity.class);
	}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        loadingActivity = getActivity();
        key = AESHelper.getKeyBytes("123456");
    }
    
    @Override
    protected void tearDown(){
    	Record.deleteAll(loadingActivity);
    }
    
    public void testPreconditions() {
        assertNotNull("loadingActivity is null", loadingActivity);
    }

    @MediumTest
    public void testFetchAllRecords() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
    	Record[] expect = {
    			new Record("中行", "randai", "dkq123", "", key),
    			new Record("农行", "randai1", "dkq123", "", key),
    			new Record("共行", "randai2", "dkq123", "", key)
    			};
    	for(Record r: expect){
    		r.add(loadingActivity);
    	}
    	List<Record> actual = Record.fetchAllRecords(loadingActivity, 0, -1, key);
    	assertEquals(expect.length, actual.size());
    	for(int i = 0; i < expect.length; i++){
    		assertRecord(expect[i], actual.get(i));
    	}
    	
    	actual = Record.fetchAllRecords(loadingActivity, 0, 2, key);
    	assertEquals(2, actual.size());
    	for(int i = 0; i < 2; i++){
    		assertRecord(expect[i], actual.get(i));
    	}
    }

    @MediumTest
    public void testFetchByTag() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
    	Record[] expect = {
    			new Record("中行", "randai", "dkq123", "", key),
    			new Record("中行", "randai1", "dkq123", "", key),
    			new Record("工行", "randai2", "dkq123", "", key)
    			};
    	for(Record r: expect){
    		r.add(loadingActivity);
    	}
    	List<Record> actual = Record.fetchByTag(loadingActivity, 0, -1, key, "中行");
    	assertEquals(2, actual.size());
    	for(int i = 0; i < 2; i++){
    		assertRecord(expect[i], actual.get(i));
    	}
    	
    	actual = Record.fetchByTag(loadingActivity, 0, 1, key, "中行");
    	assertEquals(1, actual.size());
    	for(int i = 0; i < 1; i++){
    		assertRecord(expect[i], actual.get(i));
    	}
    }

    @MediumTest
    public void testFetchByID() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
    	Record[] expect = {
    			new Record("中行", "randai", "dkq123", "", key),
    			new Record("中行", "randai1", "dkq123", "", key),
    			new Record("工行", "randai2", "dkq123", "", key)
    			};
    	for(Record r: expect){
    		r.add(loadingActivity);
    	}
    	Record actual = Record.fetchByID(loadingActivity, key, expect[1].getID());
    	assertRecord(expect[1], actual);
    }

    @MediumTest
    public void testUpdate() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
    	Record[] expect = {
    			new Record("中行", "randai", "dkq123", "", key),
    			new Record("中行", "randai1", "dkq123", "", key),
    			new Record("工行", "randai2", "dkq123", "", key)
    			};
    	for(Record r: expect){
    		r.add(loadingActivity);
    	}
    	expect[0].setPassword("123123", key);
    	expect[0].update(loadingActivity);
    	Record actual = Record.fetchByID(loadingActivity, key, expect[0].getID());
    	assertRecord(expect[0], actual);
    }

    @MediumTest
    public void testDelete() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
    	Record[] expect = {
    			new Record("中行", "randai", "dkq123", "", key),
    			new Record("中行", "randai1", "dkq123", "", key),
    			new Record("工行", "randai2", "dkq123", "", key)
    			};
    	for(Record r: expect){
    		r.add(loadingActivity);
    	}
    	expect[0].delete(loadingActivity);
    	List<Record> actual = Record.fetchAllRecords(loadingActivity, 0, -1, key);
    	assertEquals(2, actual.size());
    }
    
    @MediumTest
    public void testDeleteAll() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
    	Record[] expect = {
    			new Record("中行", "randai", "dkq123", "", key),
    			new Record("中行", "randai1", "dkq123", "", key),
    			new Record("工行", "randai2", "dkq123", "", key)
    			};
    	for(Record r: expect){
    		r.add(loadingActivity);
    	}
    	Record.deleteAll(loadingActivity);
    	List<Record> actual = Record.fetchAllRecords(loadingActivity, 0, -1, key);
    	assertEquals(0, actual.size());
    }
    
    private void assertRecord(Record expect, Record actual) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		assertEquals(expect.getID(), actual.getID());
    	assertEquals(expect.getTag(key), actual.getTag(key));
		assertEquals(expect.getUsername(key), actual.getUsername(key));
		assertEquals(expect.getPassword(key), actual.getPassword(key));
		assertEquals(expect.getRemarks(key), actual.getRemarks(key));
    }
    
}
