/**********************************************
 * 
 * Copyright (C) 2014  Moonshile (moonshile@foxmail.com)
 *
 **********************************************/

/**
 * @Title SQLiteFactory.java
 * @Package com.moonshile.helper
 * @author Duan Kaiqiang (段凯强)
 * @date 2014-7-18
 * @update 
 *
 */

package com.moonshile.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author duankq
 *
 */
public class SQLiteFactory  extends SQLiteOpenHelper{

	/********************************** Constructor ********************************************/
	
	private SQLiteFactory(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/********************************** Methods ********************************************/

	public static SQLiteFactory getInstance(Context context){
		if(theSQLite == null){
			theSQLite = new SQLiteFactory(context);
		}
		return theSQLite;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_RECORD);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO do nothing
		return;
	}
	
    public static void dropAll(SQLiteDatabase wdb){
        wdb.execSQL(SQL_DROP_RECORD);
    }
	
	/********************************** Fields ********************************************/
	 //field for implementation of singleton
    private static SQLiteFactory theSQLite = null;
	
	//---------------------------constants for sqlite helpr--------------------------------------
	
	public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "com.moonshile.Failword.db";
    
    //---------------------------constants for SQL statements------------------------------------
	
  	//data types
  	private static final String TEXT_TYPE = " TEXT";
//  	private static final String NULL_TYPE = " NULL";
//  	private static final String INTEGER_TYPE = " INTEGER";
//  	private static final String REAL_TYPE = " REAL";
//  	private static final String BLOB_TYPE = " BLOB";
//  	private static final String DATETIME_TYPE = " DATETYPE";
  	
  	//comma
  	private static final String COMMA_SEP = ",";
  	
  	//create tables
  	private static final String SQL_CREATE_RECORD =
  			"CREATE TABLE IF NOT EXISTS " + Contract.RecordSchema.TABLE_NAME + " (" +
  					Contract.RecordSchema._ID + " INTEGER PRIMARY KEY," +
  					Contract.RecordSchema.COLUMN_NAME_TAG + TEXT_TYPE + COMMA_SEP + 
  					Contract.RecordSchema.COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
  					Contract.RecordSchema.COLUMN_NAME_PASSWORD + TEXT_TYPE + COMMA_SEP +
  					Contract.RecordSchema.COLUMN_NAME_REMARKS + TEXT_TYPE + 
  					 " )";
  	
 
  	//delete tables
  	private static final String SQL_DROP_RECORD =
  		    "DROP TABLE IF EXISTS " + Contract.RecordSchema.TABLE_NAME;


}
