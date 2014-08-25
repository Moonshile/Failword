/**********************************************
 * 
 * Copyright (C) 2014  Moonshile (moonshile@foxmail.com)
 *
 **********************************************/

package com.moonshile.helper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class MessageTypes {

	public static final int MSG_IMPORT_FAIL = -2;
	public static final int MSG_ERROR = -1;
	public static final int MSG_XML_RECORD_COUNT_AND_VERSION = 0;
	public static final String MSG_DATA_RECORD_COUNT = "MSG_DATA_RECORD_COUNT";
	public static final String MSG_DATA_RECORD_VERSION = "MSG_DATA_RECORD_VERSION";
	public static final int MSG_XML_READ_RECORDS_COUNT = 1;
	public static final String MSG_DATA_READ_RECORDS_COUNT = "MSG_DATA_READ_RECORDS_COUNT";
	public static final int MSG_XML_FINISH = 2;
	public static final int MSG_IMPORT_START = 3;
	public static final String MSG_DATA_IMPORT_COUNT = "MSG_IMPORT_COUNT";
	public static final int MSG_IMPORTED_COUNT = 4;
	public static final String MSG_DATA_IMPORTED_COUNT = "MSG_DATA_IMPORTED_COUNT";
	public static final int MSG_IMPORT_FINISH = 5;
	public static final int MSG_MERGE_START = 6;
	public static final String MSG_DATA_MERGE_COUNT = "MSG_DATA_MERGE_COUNT";
	public static final int MSG_MERGED_COUNT = 7;
	public static final String MSG_DATA_MERGED_COUNT = "MSG_DATA_MERGED_COUNT";
	public static final int MSG_MERGE_FINISH = 8;

	
	public static void sendMessage(int what, String[] dataNames, String[] dataValues, Handler handler){
		Message msg = new Message();
		msg.what = what;
		if(dataNames != null && dataValues != null && dataNames.length == dataValues.length){
			Bundle data = new Bundle();
			for(int i = 0; i < dataNames.length; i++){
				data.putString(dataNames[i], dataValues[i]);
			}
			msg.setData(data);
		}
		handler.sendMessage(msg);
	}
}
