
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

import android.provider.BaseColumns;

public class Contract {

	/********************************** Constructor ********************************************/

	// private constructor to prevent initialization of this class
	private Contract(){	}
	
	
	public static abstract class RecordSchema implements BaseColumns{
		public static final String TABLE_NAME = "Record";
		public static final String COLUMN_NAME_TAG = "Tag";
		public static final String COLUMN_NAME_USERNAME = "Username";
		public static final String COLUMN_NAME_PASSWORD = "Password";
		public static final String COLUMN_NAME_REMARKS = "RMARKS";
	}
}
