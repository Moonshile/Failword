/**
 * @Title Record.java
 * @Package com.moonshile.storage
 * @author Duan Kaiqiang (段凯强)
 * @date 2014-7-17
 * @update 
 *
 */
package com.moonshile.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.moonshile.helper.AESHelper;
import com.moonshile.helper.Contract;
import com.moonshile.helper.SQLiteFactory;

/**
 * @author duankq
 *
 */
public class Record implements Serializable {
	

	/********************************** Constructor ********************************************/

	private static final long serialVersionUID = 5661856778526643899L;

	public static final int NOT_ADDED = -1;
	
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
		_id = NOT_ADDED;
		_tag = AESHelper.encrypt(tag, key);
		_username = AESHelper.encrypt(username, key);
		_password = AESHelper.encrypt(password, key);
		_remarks = AESHelper.encrypt(remarks, key);
	}
	
	public Record(){
		_id = -1;
	}
	
	/********************************** Methods ********************************************/
	
	/**
	 * Add to database, if the record has already been inserted, then update it
	 */
	public void add(Context context){
		if(_id < 0){
			SQLiteDatabase db = SQLiteFactory.getInstance(context).getWritableDatabase();
			_id = db.insert(Contract.RecordSchema.TABLE_NAME, null, getValues());
			db.close();
		}else{
			update(context);
		}
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
	
	/**
	 * export records to sdcard
	 * @param records
	 * @param key
	 * @return path of exported file
	 */
	@SuppressLint("SimpleDateFormat")
	public static String exportRecords(List<Record> records) 
			throws IOException, XmlPullParserException, InvalidKeyException, IllegalArgumentException, 
			IllegalStateException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, 
			BadPaddingException, NoSuchProviderException{
		String path = Environment.getExternalStorageDirectory().getPath()
				+ "/Failword";
		File f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
		path += "/" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".failword";
		f = new File(path);
		if (!f.exists()) {
			f.createNewFile();
		}
		FileOutputStream fout = new FileOutputStream(f, false);
		XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
		XmlSerializer serializer = pullParserFactory.newSerializer();
		serializer.setOutput(fout, "utf-8");
		serializer.startDocument("utf-8", true);
		//startTag(namespace, tagName)
		serializer.startTag(null, XML_ROOT);
		for(Record record: records){
			serializer.startTag(null, XML_RECORD);
			
			serializer.startTag(null, XML_RECORD_TAG);
			serializer.text(record.getTag());
			serializer.endTag(null, XML_RECORD_TAG);
			
			serializer.startTag(null, XML_RECORD_USERNAME);
			serializer.text(record.getUsername());
			serializer.endTag(null, XML_RECORD_USERNAME);
			
			serializer.startTag(null, XML_RECORD_PASSWORD);
			serializer.text(record.getPassword());
			serializer.endTag(null, XML_RECORD_PASSWORD);
			
			serializer.startTag(null, XML_RECORD_REMARKS);
			serializer.text(record.getRemarks());
			serializer.endTag(null, XML_RECORD_REMARKS);
			
			serializer.endTag(null, XML_RECORD);
		}
		serializer.endTag(null, XML_ROOT);
		serializer.endDocument();
		//fout.flush();
		fout.close();
		return path;
	}
	
	public static List<Record> importRecords(String path) throws XmlPullParserException, IOException{
		List<Record> res = new ArrayList<Record>();
		File f = new File(path);
		if(f.exists() && f.isFile()){
			FileInputStream fin = new FileInputStream(f);
			XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();
			parser.setInput(fin, "utf-8");
			int eventType = parser.getEventType();
			Record record = null;
			while(eventType != XmlPullParser.END_DOCUMENT){
				switch(eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String name = parser.getName();
					if(name.equals(XML_RECORD)){
						record = new Record();
					}else if(name.equals(XML_RECORD_TAG)){
						record.setTag(parser.nextText());
					}else if(name.equals(XML_RECORD_USERNAME)){
						record.setUsername(parser.nextText());
					}else if(name.equals(XML_RECORD_PASSWORD)){
						record.setPassword(parser.nextText());
					}else if(name.equals(XML_RECORD_REMARKS)){
						record.setRemarks(parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if(parser.getName().equals(XML_RECORD) && record != null){
						res.add(record);
						record = null;
					}
					break;
				}
				eventType = parser.next();
			}
		}
		return res;
	}
	
	/**
	 * import records from sdcard with default path, this method won't insert the records into database
	 * @return the records
	 */
	public static List<Record> importRecords() 
			throws IOException, XmlPullParserException, InvalidKeyException, 
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, NoSuchProviderException{
		List<Record> res = new ArrayList<Record>();
		String path = Environment.getExternalStorageDirectory().getPath()
				+ "/Failword";
		File f = new File(path);
		if(f.exists() && f.isDirectory()){
			File[] files = f.listFiles();
			f = new File(path + "/0000");
			for(File file: files){
				if(file.getCanonicalPath().compareTo(f.getCanonicalPath()) > 0){
					f = file;
				}
			}
			return importRecords(f.getCanonicalPath());
		}
		return res;
	}
	
	/**
	 * merge same records in database, but won't influence records given
	 * @return same records should to be removed in given records
	 */
	public static List<Record> mergeRecords(List<Record> records, Context context){
		List<Record> toRm = new ArrayList<Record>();
		if(records.size() > 1){
			for(int i = 1; i < records.size(); i++){
				if(records.get(i).isSame(records.get(i - 1))){
					toRm.add(records.get(i));
				}
			}
			for(Record rm: toRm){
				rm.delete(context);
			}
		}
		return toRm;
	}
	
	public boolean isSame(Record record){
		return _tag.equals(record.getTag()) && _username.equals(record.getUsername()) &&
				_password.equals(record.getPassword()) && _remarks.equals(record.getRemarks());
	}
	
	/********************************** Fields ********************************************/
	

	public static final String XML_ROOT = "failword";
	public static final String XML_RECORD = "record";
	public static final String XML_RECORD_TAG = "tag";
	public static final String XML_RECORD_USERNAME = "username";
	public static final String XML_RECORD_PASSWORD = "password";
	public static final String XML_RECORD_REMARKS = "remarks";
	
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
	public String getTag(){
		return _tag;
	}
	public void setTag(String tag){
		_tag = tag;
	}
	
	public String getUsername(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		return AESHelper.decrypt(_username, key);
	}
	public void setUsername(String username, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		_username = AESHelper.encrypt(username, key);
	}
	public String getUsername(){
		return _username;
	}
	public void setUsername(String username){
		_username = username;
	}
	
	public String getPassword(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		return AESHelper.decrypt(_password, key);
	}
	public void setPassword(String password, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		_password = AESHelper.encrypt(password, key);
	}
	public String getPassword(){
		return _password;
	}
	public void setPassword(String password){
		_password = password;
	}
	
	public String getRemarks(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		return AESHelper.decrypt(_remarks, key);
	}
	public void setRemarks(String remarks, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException{
		_remarks = AESHelper.encrypt(remarks, key);
	}
	public String getRemarks(){
		return _remarks;
	}
	public void setRemarks(String remarks){
		_remarks = remarks;
	}
	
}
