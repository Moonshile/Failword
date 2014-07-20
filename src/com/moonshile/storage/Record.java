/**
 * @Title Record.java
 * @Package com.moonshile.storage
 * @author Duan Kaiqiang (段凯强)
 * @date 2014-7-17
 * @update 
 *
 */
package com.moonshile.storage;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.moonshile.helper.AESHelper;
import com.moonshile.helper.Contract;
import com.moonshile.helper.SQLiteFactory;

/**
 * @author duankq
 *
 */
public class Record {
	

	/********************************** Constructor ********************************************/

	/**
	 * constructor
	 * @param key digest of the key given
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws NoSuchProviderException 
	 */
	public Record(String tag, String username, String password, String remarks, byte[] key) 
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, 
			IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		_id = -1;
		_tag = AESHelper.encrypt(tag, key);
		_username = AESHelper.encrypt(username, key);
		_password = AESHelper.encrypt(password, key);
		_remarks = AESHelper.encrypt(remarks, key);
	}
	
	/********************************** Methods ********************************************/
	
	/**
	 * Add to database
	 */
	public void add(Context context){
		SQLiteDatabase db = SQLiteFactory.getInstance(context).getWritableDatabase();
		_id = db.insert(Contract.RecordSchema.TABLE_NAME, null, getValues());
		db.close();
	}
	
	/**
	 * Update database for the fields updated
	 */
	public void update(Context context){
		if(_id < 0){
			return;
		}
		SQLiteDatabase db = SQLiteFactory.getInstance(context).getWritableDatabase();
		String[] whereArgs = { _id + "" };
		db.update(Contract.RecordSchema.TABLE_NAME, getValues(), Contract.RecordSchema._ID + " = ?", whereArgs);
		db.close();
	}
	
	private ContentValues getValues(){
		ContentValues values = new ContentValues();
		values.put(Contract.RecordSchema.COLUMN_NAME_TAG, _tag);
		values.put(Contract.RecordSchema.COLUMN_NAME_USERNAME, _username);
		values.put(Contract.RecordSchema.COLUMN_NAME_PASSWORD, _password);
		values.put(Contract.RecordSchema.COLUMN_NAME_REMARKS, _remarks);
		return values;
	}
	
	/**
	 * Delete the record in database
	 */
	public void delete(Context context){
		if(_id < 0){
			return;
		}
		SQLiteDatabase db = SQLiteFactory.getInstance(context).getWritableDatabase();
		String[] whereArgs = { _id + "" };
		db.delete(Contract.RecordSchema.TABLE_NAME, Contract.RecordSchema._ID + " = ?", whereArgs);
		db.close();
	}
	
	/**
	 * Delete all records in database
	 */
	public static void deleteAll(Context context){
		SQLiteDatabase db = SQLiteFactory.getInstance(context).getWritableDatabase();
		db.delete(Contract.RecordSchema.TABLE_NAME, null, null);
		db.close();
	}
	
	/**
	 * Fetch all records
	 * @param count -1 for all records
	 * @throws IllegalArgumentException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws NoSuchProviderException 
	 */
	public static List<Record> fetchAllRecords(Context context, int startIndex, int count, byte[] key) 
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, 
			IllegalBlockSizeException, BadPaddingException, IllegalArgumentException, NoSuchProviderException{
		SQLiteDatabase db = SQLiteFactory.getInstance(context).getReadableDatabase();
		String orderBy = Contract.RecordSchema.COLUMN_NAME_TAG + "," + Contract.RecordSchema.COLUMN_NAME_USERNAME + " ASC";
		Cursor c = db.query(Contract.RecordSchema.TABLE_NAME, getProjection(), null, null, null, null, orderBy);
		List<Record> res = toList(c, startIndex, count, key);
		db.close();
		return res;
	}
	
	/**
	 * Fetch the records has the specific tag
	 * @param tag is not encrypted
	 * @count -1 for all records
	 * @throws IllegalArgumentException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws NoSuchProviderException 
	 */
	public static List<Record> fetchByTag(Context context, int startIndex, int count, byte[] key, String tag) 
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, 
			IllegalBlockSizeException, BadPaddingException, IllegalArgumentException, NoSuchProviderException{
		SQLiteDatabase db = SQLiteFactory.getInstance(context).getReadableDatabase();
		String orderBy = Contract.RecordSchema.COLUMN_NAME_USERNAME + " ASC";
		String selection = Contract.RecordSchema.COLUMN_NAME_TAG + "= ?";
		String[] selectionArgs = { AESHelper.encrypt(tag, key) };
		Cursor c = db.query(Contract.RecordSchema.TABLE_NAME, getProjection(), selection, selectionArgs, null, null, orderBy);
		List<Record> res = toList(c, startIndex, count, key);
		db.close();
		return res;
	}
	
	/**
	 * Fetch the record has the specific id
	 * @throws IllegalArgumentException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws NoSuchProviderException 
	 */
	public static Record fetchByID(Context context, byte[] key, long id) 
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, 
			IllegalBlockSizeException, BadPaddingException, IllegalArgumentException, NoSuchProviderException{
		SQLiteDatabase db = SQLiteFactory.getInstance(context).getReadableDatabase();
		String selection = Contract.RecordSchema._ID + "= ?";
		String[] selectionArgs = { id + "" };
		Cursor c = db.query(Contract.RecordSchema.TABLE_NAME, getProjection(), selection, selectionArgs, null, null, null);
		List<Record> ls = toList(c, 0, -1, key);
		db.close();
		if(ls.size() == 1){
			return ls.get(0);
		}
		return null;
	}
	
	private static String[] getProjection(){
		String[]  res = {
				Contract.RecordSchema._ID,
				Contract.RecordSchema.COLUMN_NAME_TAG,
				Contract.RecordSchema.COLUMN_NAME_USERNAME,
				Contract.RecordSchema.COLUMN_NAME_PASSWORD,
				Contract.RecordSchema.COLUMN_NAME_REMARKS
		};
		return res;
	}
	
	private static List<Record> toList(Cursor c, int startIndex, int count, byte[] key) 
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, 
			IllegalBlockSizeException, BadPaddingException, IllegalArgumentException, NoSuchProviderException{
		if(count < 0){
			count = c.getCount();
		}
		List<Record> res = new ArrayList<Record>();
		if(c.getCount() > startIndex){
			c.moveToPosition(startIndex);
			for (int i = 0; i < count; i++) {
				Record r = new Record(
						AESHelper.decrypt(c.getString(c.getColumnIndexOrThrow(Contract.RecordSchema.COLUMN_NAME_TAG)), key),
						AESHelper.decrypt(c.getString(c.getColumnIndexOrThrow(Contract.RecordSchema.COLUMN_NAME_USERNAME)), key),
						AESHelper.decrypt(c.getString(c.getColumnIndexOrThrow(Contract.RecordSchema.COLUMN_NAME_PASSWORD)), key),
						AESHelper.decrypt(c.getString(c.getColumnIndexOrThrow(Contract.RecordSchema.COLUMN_NAME_REMARKS)), key),
						key);
				r.setID(c.getLong(c.getColumnIndexOrThrow(Contract.RecordSchema._ID)));
				res.add(r);
				if (c.isLast()) {
					break;
				}
				c.moveToNext();
			}
		}
		return res;
	}
	
	/********************************** Fields ********************************************/
	
	private long _id;
	private String _tag;
	private String _username;
	private String _password;
	private String _remarks;
	
	public long getID(){
		return _id;
	}
	public void setID(long id){
		_id = id;
	}
	
	public String getTag(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		return AESHelper.decrypt(_tag, key);
	}
	public void setTag(String tag, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		_tag = AESHelper.encrypt(tag, key);
	}
	
	public String getUsername(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		return AESHelper.decrypt(_username, key);
	}
	public void setUsername(String username, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		_username = AESHelper.encrypt(username, key);
	}
	
	public String getPassword(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		return AESHelper.decrypt(_password, key);
	}
	public void setPassword(String password, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		_password = AESHelper.encrypt(password, key);
	}
	
	public String getRemarks(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		return AESHelper.decrypt(_remarks, key);
	}
	public void setRemarks(String remarks, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		_remarks = AESHelper.encrypt(remarks, key);
	}
}
