/**********************************************
 * 
 * Copyright (C) 2014  Moonshile (moonshile@foxmail.com)
 *
 **********************************************/

package com.moonshile.failword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;


import com.moonshile.helper.AppIcon;
import com.moonshile.helper.MessageTypes;
import com.moonshile.helper.MoonshileSort;
import com.moonshile.helper.Resource;
import com.moonshile.storage.Record;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private byte[] key;
	private MainGridAdapterHelper adapterHelper; 
	private List<String> tags;
	private SimpleAdapter adapter;
	private ArrayAdapter<String> tagAdapter;
	private final MainActivity context = this;
	private Handler dataHandler; // for handling import or export of data
	private boolean onResumeLock = false; // whether lock the screen while onResume or not

	private static final String RECORD_ID = "RECORD_ID";
	private static final String RECORD_TAG = "RECORD_TAG";
	private static final String RECORD_ICON = "RECORD_ICON";
	private static final String RECORD_USERNAME = "RECORD_USERNAME";
	
	public static final String INTENT_RECORD_EDITED = "INTENT_RECORD_EDITED";
	public static final String INTENT_KEY = "INTENT_KEY";
	public static final String INTENT_RECORDS = "INTENT_RECORDS";
	
	public static final int REQUEST_CODE_EDIT = 0;
	public static final int REQUEST_CODE_DETAIL = 1;
	public static final int REQUEST_CODE_CHANGE = 2;
	public static final int REQUEST_CODE_LOCK = 3;
	public static final int REQUEST_CODE_ABOUT = 4;
	
	public static final int IMPORTING_INC = 1;
	public static final int MERGING_INC = 20;
	
	
	@SuppressLint("HandlerLeak")
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent intent = getIntent();
		key = intent.getByteArrayExtra(LoadingActivity.INTENT_KEY);
		adapterHelper = new MainGridAdapterHelper((List<Record>)intent.getSerializableExtra(LoadingActivity.INTENT_RECORDS), this);
		adapter = adapterHelper.getAdapter(null, key);
		GridView gridView = ((GridView)findViewById(R.id.main_grid_records));
		gridView.setAdapter(adapter);
		// show detail of a record while click items in gridview
		gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(context, DetailActivity.class);
				intent.putExtra(INTENT_KEY, key);
				intent.putExtra(MainActivity.INTENT_RECORD_EDITED, adapterHelper.getRecords().get(position));
				context.startActivityForResult(intent, MainActivity.REQUEST_CODE_DETAIL);
			}
			
		});

		// show candidate records while searching
		tags = new ArrayList<String>();
		updateTags();
		AutoCompleteTextView auto = ((AutoCompleteTextView)findViewById(R.id.main_search_text));
		auto.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				String tag = ((TextView)view).getText().toString();
				adapter = adapterHelper.getAdapter(tag, key);
				((GridView)findViewById(R.id.main_grid_records)).setAdapter(adapter);
			}
			
		});
		auto.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				String tag = ((AutoCompleteTextView)view).getText().toString();
				if(keyCode == KeyEvent.KEYCODE_DEL && tag.equals("")){
					adapter = adapterHelper.getAdapter(null, key);
					((GridView)findViewById(R.id.main_grid_records)).setAdapter(adapter);
					return true;
				}
				return false;
			}
			
		});
		
		// TODO deal with handler leak
		dataHandler = new Handler(){
			private int total = -1;
			private int c = 0;
			
			@Override
			public void handleMessage(Message msg){
				TextView barHint = (TextView)findViewById(R.id.main_import_progress_text);
				LinearLayout importHint = (LinearLayout)findViewById(R.id.main_import_data_dialog);
				int curCount = 0;
				switch(msg.what){
				case MessageTypes.MSG_XML_RECORD_COUNT_AND_VERSION:
					total = Integer.parseInt(msg.getData().getString(MessageTypes.MSG_DATA_RECORD_COUNT));
					importHint.setVisibility(View.VISIBLE);
					break;
				case MessageTypes.MSG_XML_READ_RECORDS_COUNT:
					curCount = Integer.parseInt(msg.getData().getString(MessageTypes.MSG_DATA_READ_RECORDS_COUNT));
					barHint.setText(context.getResources().getString(R.string.main_import_progress_text_reading) + 
							curCount + "/" + (total > -1 ? total + "" : 
								context.getResources().getString(R.string.main_import_progress_count_unknown)));
					break;
				case MessageTypes.MSG_XML_FINISH:
					total = -1;
					break;
				case MessageTypes.MSG_IMPORT_START:
					total = Integer.parseInt(msg.getData().getString(MessageTypes.MSG_DATA_IMPORT_COUNT));
					break;
				case MessageTypes.MSG_IMPORTED_COUNT:
					c += IMPORTING_INC;
					barHint.setText(context.getResources().getString(R.string.main_import_progress_text_importing) + 
							c + "/" + total);
					break;
				case MessageTypes.MSG_IMPORT_FINISH:
					total = -1;
					c = 0;
					barHint.setText(context.getResources().getString(
							R.string.main_import_progress_text_merging).replace(":", "...").replace("：", "..."));
					break;
				case MessageTypes.MSG_MERGE_START:
					total = Integer.parseInt(msg.getData().getString(MessageTypes.MSG_DATA_MERGE_COUNT));
					break;
				case MessageTypes.MSG_MERGED_COUNT:
					c += MERGING_INC;
					barHint.setText(context.getResources().getString(R.string.main_import_progress_text_merging) + 
							c + "/" + total);
					break;
				case MessageTypes.MSG_MERGE_FINISH:
					adapter.notifyDataSetChanged();
					updateTags();
					if(total > 0){
						Toast.makeText(context, R.string.main_merged, Toast.LENGTH_SHORT).show();
					}
					reset(importHint);
					break;
				case MessageTypes.MSG_ERROR:
					reset(importHint);
					Toast.makeText(context, R.string.error_hint, Toast.LENGTH_SHORT).show();
					break;
				case MessageTypes.MSG_IMPORT_FAIL:
					reset(importHint);
					Toast.makeText(context, R.string.main_import_error_hint, Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
			private void reset(LinearLayout importHint){
				importHint.setVisibility(View.GONE);
				total = -1;
				c = 0;
			}
		};

		// if open a exported file with failword
		importFromOpenFile(intent);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		// lock screen if app is paused
		if(onResumeLock){
			Intent lock = new Intent(this, LockActivity.class);
			lock.putExtra(INTENT_KEY, key);
			this.startActivityForResult(lock, MainActivity.REQUEST_CODE_LOCK);
		}
		onResumeLock = true;
	}
	
	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		// if open a exported file with failword
		importFromOpenFile(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		switch(requestCode){
		case REQUEST_CODE_EDIT:
			switch(resultCode){
			case EditActivity.RESULT_OK:
				if(intent == null){
					// intent is null, so operation is canceled.
					Toast.makeText(this, R.string.cancel_hint, Toast.LENGTH_SHORT).show();
				}else{
					Record record = (Record)intent.getSerializableExtra(INTENT_RECORD_EDITED);
					adapterHelper.insertOrUpdate(record, true);
					// update gridview
					adapter.notifyDataSetChanged();
					updateTags();
					Toast.makeText(this, R.string.main_edit_ok, Toast.LENGTH_SHORT).show();
				}
				break;
			case EditActivity.RESULT_ERROR:
				Toast.makeText(this, R.string.error_hint, Toast.LENGTH_SHORT).show();
				break;
			case EditActivity.RESULT_CANCELED:
				Toast.makeText(this, R.string.cancel_hint, Toast.LENGTH_SHORT).show();
				break;
			}
			break;
		case REQUEST_CODE_DETAIL:
			switch(resultCode){
			case DetailActivity.RESULT_OK:
				if(intent != null){
					Record record = (Record)intent.getSerializableExtra(INTENT_RECORD_EDITED);
					adapterHelper.insertOrUpdate(record, true);
					// update gridview
					adapter.notifyDataSetChanged();
					updateTags();
				}
				break;
			case DetailActivity.RESULT_ERROR:
				Toast.makeText(this, R.string.error_hint, Toast.LENGTH_SHORT).show();
				break;
			case DetailActivity.RESULT_CANCELED:
				break;
			case DetailActivity.RESULT_DELETED:
				if(intent != null){
					Record record = (Record)intent.getSerializableExtra(INTENT_RECORD_EDITED);
					adapterHelper.delete(record);
					adapter.notifyDataSetChanged();
					updateTags();
				}
				break;
			}
			break;
		case REQUEST_CODE_CHANGE:
			switch(resultCode){
			case ChangeKeyActivity.RESULT_OK:
				Toast.makeText(this, R.string.main_change_ok, Toast.LENGTH_SHORT).show();
				this.finish();
				break;
			case ChangeKeyActivity.RESULT_CANCELED:
				break;
			case ChangeKeyActivity.RESULT_ERROR:
				Toast.makeText(this, R.string.main_change_error, Toast.LENGTH_SHORT).show();
				onImport(null);
				break;
			}
			break;
		case REQUEST_CODE_LOCK:
			switch(resultCode){
			case LockActivity.RESULT_OK:
				break;
			case LockActivity.RESULT_CANCELED:
				this.finish();
				break;
			}
			break;
		}
		onResumeLock = resultCode == LockActivity.RESULT_LOCK ? true : false;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
		case R.id.main_action_add_record:
			Intent intent = new Intent(this, EditActivity.class);
			intent.putExtra(INTENT_KEY, key);
			this.startActivityForResult(intent, REQUEST_CODE_EDIT);
			break;
		case R.id.main_action_export:
			onExport();
			break;
		case R.id.main_action_import:
			onImport(null);
			break;
		case R.id.main_action_merge:
			onMerge();
			break;
		case R.id.main_action_change_key:
			Intent intentChange = new Intent(this, ChangeKeyActivity.class);
			intentChange.putExtra(INTENT_KEY, key);
			intentChange.putExtra(INTENT_RECORDS, new ArrayList<Record>(adapterHelper.getRecordsBase()));
			this.startActivityForResult(intentChange, REQUEST_CODE_CHANGE);
			break;
		case R.id.main_action_about:
			Intent intentAbout = new Intent(this, AboutActivity.class);
			this.startActivityForResult(intentAbout, REQUEST_CODE_ABOUT);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void importFromOpenFile(Intent intent){
		// if open a exported file with failword
		String path = intent.getStringExtra(LoadingActivity.INTENT_IMPORT_PATH);
		if(path != null){
			onImport(path);
			intent.removeExtra(LoadingActivity.INTENT_IMPORT_PATH);
			onResumeLock = false;
		}
	}
	
	private void onExport(){
		Handler handler = new Handler();
		handler.post(new Runnable(){

			@Override
			public void run() {
				try {
					String path = Record.exportRecords(adapterHelper.getRecordsBase());
					Toast.makeText(context, getResources().getString(R.string.main_export_ok) + path, Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Toast.makeText(context, R.string.error_hint, Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					Log.e("export", e.toString());
				}
			}
			
		});
	}
	
	private void onImport(String importPath){
		
		final String import_path = importPath;
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					List<Record> importedRecords = (import_path == null ? 
							Record.importRecords(dataHandler) : Record.importRecords(import_path, dataHandler));
					MessageTypes.sendMessage(MessageTypes.MSG_IMPORT_START, 
							new String[]{MessageTypes.MSG_DATA_IMPORT_COUNT}, 
							new String[]{importedRecords.size() + ""}, dataHandler);
					for(int i = 0; i < importedRecords.size(); i++){
						Record r = importedRecords.get(i);
						r.getTag(key); // validation key
						r.add(context);
						adapterHelper.insertOrUpdate(r, false);
						if(i % IMPORTING_INC == 0){
							MessageTypes.sendMessage(MessageTypes.MSG_IMPORTED_COUNT, null, null, dataHandler);
						}
					}
					MessageTypes.sendMessage(MessageTypes.MSG_IMPORT_FINISH, null, null, dataHandler);
					
					List<Record> toRm = Record.mergeRecords(adapterHelper.getRecordsBase(), context);
					MessageTypes.sendMessage(MessageTypes.MSG_MERGE_START, 
							new String[]{MessageTypes.MSG_DATA_MERGE_COUNT}, 
							new String[]{toRm.size() + ""}, dataHandler);
					for(int i = 0; i < toRm.size(); i++){
						Record r = toRm.get(i);
						adapterHelper.delete(r);
						if((i + 1) % MERGING_INC == 0){
							MessageTypes.sendMessage(MessageTypes.MSG_MERGED_COUNT, null, null, dataHandler);
						}
					}
					MessageTypes.sendMessage(MessageTypes.MSG_MERGE_FINISH, null, null, dataHandler);
				} catch (BadPaddingException e){
					MessageTypes.sendMessage(MessageTypes.MSG_IMPORT_FAIL, null, null, dataHandler);
				} catch (Exception e) {
					MessageTypes.sendMessage(MessageTypes.MSG_ERROR, null, null, dataHandler);
					e.printStackTrace();
					Log.e("import", e.toString());
				}
			}
			
		});
		thread.start();
	}
	
	private void onMerge(){
		Handler handler = new Handler();
		handler.post(new Runnable(){

			@Override
			public void run() {
				List<Record> toRm = Record.mergeRecords(adapterHelper.getRecordsBase(), context);
				for(Record r: toRm){
					adapterHelper.delete(r);
				}
				adapter.notifyDataSetChanged();
				updateTags();
			}
			
		});
	}
	
	private void updateTags(){
		tags.removeAll(tags);
		for(Record r: adapterHelper.getRecordsBase()){
			try {
				if(!tags.contains(r.getTag(key))){
					tags.add(r.getTag(key));
				}
			} catch (Exception e) {
				Toast.makeText(this, R.string.error_hint, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
		tagAdapter = new ArrayAdapter<String>(this, R.layout.drop_down_item, tags);
		((AutoCompleteTextView)findViewById(R.id.main_search_text)).setAdapter(tagAdapter);
	}
	

	
	
	
	
	
	
	
	
	
	
	public final class MainGridAdapterHelper {

		/********************************** Constructor ********************************************/

		public MainGridAdapterHelper(List<Record> base, MainActivity context){
			this.context = context;
			records_base = new LinkedList<Record>(base);
			records = new LinkedList<Record>(base);
			recordMapList = convertRecordsToMapList(records);
			sortAllRecords();
		}
		
		/********************************** Methods ********************************************/

		public SimpleAdapter getAdapter(String tag, byte[] key){
			records.removeAll(records);
			if(tag != null){
				for(Record r: records_base){
					try {
						if(AppIcon.getStandardName(r.getTag(key)).equals(AppIcon.getStandardName(tag))){
							records.add(r);
						}
					} catch (Exception e) {
						Toast.makeText(context, R.string.error_hint, Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				}
			}else{
				records.addAll(records_base);
			}
			recordMapList.removeAll(recordMapList);
			recordMapList.addAll(convertRecordsToMapList(records));
			return new SimpleAdapter(context, recordMapList, R.layout.grid_item_main, 
					new String[] {RECORD_TAG, RECORD_USERNAME, RECORD_ICON},
					new int[] {R.id.grid_item_tag, R.id.grid_item_username, R.id.grid_item_image});
		}
		
		public void insertOrUpdate(Record r, boolean sort){
			if(!existsAndUpdateRecords(r, sort)){
				insertIntoRecords(r, sort);
			}
		}
		
		public void delete(Record r){
			int records_index = -1;
			int records_base_index = -1;
			for(int i = 0; i < records.size(); i++){
				if(r.getID() == records.get(i).getID()){
					records_index = i;
				}
			}
			for(int i = 0; i < records_base.size(); i++){
				if(records_base.get(i).getID() == r.getID()){
					records_base_index = i;
				}
			}
			if(records_index > -1){
				records.remove(records_index);
				recordMapList.remove(records_index);
			}
			if(records_base_index > -1){
				records_base.remove(records_base_index);
			}
		}
		
		private boolean existsAndUpdateRecords(Record r, boolean sort){
			int records_index = -1;
			int records_base_index = -1;
			for(int i = 0; i < records.size(); i++){
				if(r.getID() == records.get(i).getID()){
					records_index = i;
				}
			}
			for(int i = 0; i < records_base.size(); i++){
				if(records_base.get(i).getID() == r.getID()){
					records_base_index = i;
				}
			}
			if(records_index > -1 && records_base_index > -1){
				records_base.set(records_base_index, r);
				records.set(records_index, r);
				recordMapList.set(records_index, this.convertRecordsToMap(r));
				if(sort){
					sortAllRecords();
				}
				return true;
			}
			return false;
		}
		
		/**
		 * insert a record into both records-list and map-list (after convertion)
		 */
		private void insertIntoRecords(Record r, boolean sort){
			records_base.add(findInsertIndex(records_base, r), r);
			int index = findInsertIndex(records, r);
			records.add(index, r);
			recordMapList.add(index, this.convertRecordsToMap(r));
		}
		
		private int findInsertIndex(List<Record> rs, Record r){
			RecordCmp recordCmp = new RecordCmp();
			int start = 0;
			int end = rs.size() - 1;
			if(start > end){
				return 0;
			}
			while(start != end){
				int mid = (start + end)/2;
				if(recordCmp.cmp(r, rs.get(mid)) < 0){
					end = mid;
				}else{
					start = mid + 1;
				}
			}
			return recordCmp.cmp(r, rs.get(start)) < 0 ? start : start + 1;
		}
		
		/**
		 * sort all records in records_base, records and recordMapList
		 */
		public void sortAllRecords(){
			sortRecords(records_base);
			sortRecords(records);
			sortRecordMapList(recordMapList);
		}

		private List<Map<String, Object>> convertRecordsToMapList(List<Record> rs){
			List<Map<String, Object>> rml = new LinkedList<Map<String, Object>>();
			for(Record r: rs){
				rml.add(this.convertRecordsToMap(r));
			}
			return rml;
		}
		
		/**
		 * convert a record to map object
		 */
		private Map<String, Object> convertRecordsToMap(Record r){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(RECORD_ID, r.getID());
			try {
				map.put(RECORD_TAG, r.getTag(key));
				map.put(RECORD_ICON, 
						Resource.getDrawableResByName(R.class, AppIcon.getIconName(r.getTag(key))));
				map.put(RECORD_USERNAME, r.getUsername(key));
			} catch (Exception e) {
				Toast.makeText(context, R.string.error_hint, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			return map;
		}
		
		
		
		private void sortRecords(List<Record> rs){
			MoonshileSort.mergeSort(new MoonshileSort.MoonshileList<List<Record>, Record>(){

				@Override
				public Record get(List<Record> list, int i) {
					return list.get(i);
				}

				@Override
				public void set(List<Record> list, Record f, int i) {
					list.set(i, f);
				}
				
			}, rs, 0, rs.size() - 1, new RecordCmp());
		}

		
		private void sortRecordMapList(List<Map<String, Object>> mapList){
			MoonshileSort.mergeSort(new MoonshileSort.MoonshileList<List<Map<String, Object>>, Map<String, Object>>() {

				@Override
				public Map<String, Object> get(List<Map<String, Object>> list, int i) {
					return list.get(i);
				}

				@Override
				public void set(List<Map<String, Object>> list,
						Map<String, Object> f, int i) {
					list.set(i, f);
				}
			}, mapList, 0, mapList.size() - 1, new MapCmp());
		}
		
		private class RecordCmp implements MoonshileSort.Compare<Record>{

			@Override
			public int cmp(Record f1, Record f2) {
				int res;
				try {
					res = AppIcon.getStandardName(f1.getTag(key)).compareToIgnoreCase(AppIcon.getStandardName(f2.getTag(key)));
					if(res != 0){
						return res;
					}else{
						// if their standard names are same, then use their true names
						res = f1.getTag(key).compareToIgnoreCase(f2.getTag(key));
						if(res != 0){
							return res;
						}
						return f1.getUsername(key).compareToIgnoreCase(f2.getUsername(key));
					}
				} catch (Exception e) {
					Toast.makeText(context, R.string.error_hint, Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					context.finish();
				}
				return -1;
			}
			
		}
		
		private class MapCmp implements MoonshileSort.Compare<Map<String, Object>>{

			@Override
			public int cmp(Map<String, Object> f1, Map<String, Object> f2) {
				int res;
				res = AppIcon.getStandardName((String)f1.get(RECORD_TAG)).compareToIgnoreCase(AppIcon.getStandardName((String)f2.get(RECORD_TAG)));
				if(res != 0){
					return res;
				}else{
					// if their standard names are same, then use their true names
					res = ((String)f1.get(RECORD_TAG)).compareToIgnoreCase((String)f2.get(RECORD_TAG));
					if(res != 0){
						return res;
					}
					return ((String)f1.get(RECORD_USERNAME)).compareToIgnoreCase((String)f2.get(RECORD_USERNAME));
				}
			}
			
		}
		
		/********************************** Fields ********************************************/

		private List<Record> records_base;
		public List<Record> getRecordsBase(){
			return records_base;
		}
		private List<Record> records;
		public List<Record> getRecords(){
			return records;
		}
		private List<Map<String, Object>> recordMapList;
		private MainActivity context;
	}
}
