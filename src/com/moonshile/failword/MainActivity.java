package com.moonshile.failword;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


import com.moonshile.helper.AppIcon;
import com.moonshile.helper.MoonshileSort;
import com.moonshile.helper.Resource;
import com.moonshile.storage.Record;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
		gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(context, DetailActivity.class);
				intent.putExtra(INTENT_KEY, key);
				intent.putExtra(MainActivity.INTENT_RECORD_EDITED, adapterHelper.getRecordsBase().get(position));
				context.startActivityForResult(intent, MainActivity.REQUEST_CODE_DETAIL);
			}
			
		});

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

		String path = intent.getStringExtra(LoadingActivity.INTENT_IMPORT_PATH);
		if(path != null){
			onImport(path);
		}
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
					adapterHelper.insertOrUpdate(record);
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
					adapterHelper.insertOrUpdate(record);
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
		}
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
			intentChange.putExtra(INTENT_RECORDS, (ArrayList<Record>)adapterHelper.getRecordsBase());
			this.startActivityForResult(intentChange, REQUEST_CODE_CHANGE);
			break;
		case R.id.main_action_about:
			Intent intentAbout = new Intent(this, AboutActivity.class);
			this.startActivity(intentAbout);
			break;
		}
		return super.onOptionsItemSelected(item);
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
		Handler handler = new Handler();
		final String import_path = importPath;
		handler.post(new Runnable(){

			@Override
			public void run() {
				try {
					List<Record> importedRecords = (import_path == null ? Record.importRecords() : Record.importRecords(import_path));
					for(Record r: importedRecords){
						r.getTag(key); // validation key
						r.add(context);
						adapterHelper.insertOrUpdate(r);
					}
					List<Record> toRm = Record.mergeRecords(adapterHelper.getRecordsBase(), context);
					for(Record r: toRm){
						adapterHelper.delete(r);
					}
					if(toRm.size() > 0){
						Toast.makeText(context, R.string.main_merged, Toast.LENGTH_SHORT).show();
					}
					adapter.notifyDataSetChanged();
					updateTags();
				} catch (BadPaddingException e){
					Toast.makeText(context, R.string.main_import_error_hint, Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Toast.makeText(context, R.string.error_hint, Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					Log.e("import", e.toString());
				}
			}
			
		});
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
			} catch (InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | NoSuchProviderException e) {
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
			records_base = base;
			records = new ArrayList<Record>();
			records.addAll(base);
			recordMapList = convertRecordsToMapList(records);
			sortRecords(records_base);
			sortRecords(records);
			sortRecordMapList(recordMapList);
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
					} catch (InvalidKeyException | NoSuchAlgorithmException
							| NoSuchPaddingException
							| IllegalBlockSizeException | BadPaddingException
							| NoSuchProviderException e) {
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
		
		public void insertOrUpdate(Record r){
			if(!existsAndUpdateRecords(r)){
				insertIntoRecords(r);
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
		
		private boolean existsAndUpdateRecords(Record r){
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
				sortRecords(records_base);
				sortRecords(records);
				sortRecordMapList(recordMapList);
				return true;
			}
			return false;
		}
		
		/**
		 * insert a record into both records-list and map-list (after convertion)
		 */
		private void insertIntoRecords(Record r){
			records_base.add(r);
			records.add(r);
			recordMapList.add(this.convertRecordsToMap(r));
			sortRecords(records_base);
			sortRecords(records);
			sortRecordMapList(recordMapList);
		}

		private List<Map<String, Object>> convertRecordsToMapList(List<Record> rs){
			List<Map<String, Object>> rml = new ArrayList<Map<String, Object>>();
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
			} catch (InvalidKeyException | NotFoundException
					| IllegalAccessException | IllegalArgumentException
					| NoSuchAlgorithmException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException
					| NoSuchProviderException e) {
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
				
			}, rs, 0, rs.size() - 1, new MoonshileSort.Compare<Record>(){

				@Override
				public int cmp(Record f1, Record f2) {
					int res;
					try {
						res = AppIcon.getStandardName(f1.getTag(key)).compareTo(AppIcon.getStandardName(f2.getTag(key)));
						if(res != 0){
							return res;
						}else{
							return f1.getUsername(key).compareTo(f2.getUsername(key));
						}
					} catch (InvalidKeyException | NoSuchAlgorithmException
							| NoSuchPaddingException | IllegalBlockSizeException
							| BadPaddingException | NoSuchProviderException e) {
						Toast.makeText(context, R.string.error_hint, Toast.LENGTH_SHORT).show();
						e.printStackTrace();
						context.finish();
					}
					return -1;
				}
				
			});
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
			}, mapList, 0, mapList.size() - 1, new MoonshileSort.Compare<Map<String, Object>>() {

				@Override
				public int cmp(Map<String, Object> f1, Map<String, Object> f2) {
					int res;
					res = AppIcon.getStandardName((String)f1.get(RECORD_TAG)).compareTo(AppIcon.getStandardName((String)f2.get(RECORD_TAG)));
					if(res != 0){
						return res;
					}else{
						return ((String)f1.get(RECORD_USERNAME)).compareTo((String)f2.get(RECORD_USERNAME));
					}
				}
			});
		}
		/********************************** Fields ********************************************/

		private List<Record> records_base;
		public List<Record> getRecordsBase(){
			return records_base;
		}
		private List<Record> records;
		private List<Map<String, Object>> recordMapList;
		private MainActivity context;
	}
}
