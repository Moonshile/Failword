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

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	
	private List<Record> records;
	private byte[] key;
	private List<Map<String, Object>> recordMapList;
	private SimpleAdapter adapter;
	private final MainActivity context = this;

	private static final String RECORD_ID = "RECORD_ID";
	private static final String RECORD_TAG = "RECORD_TAG";
	private static final String RECORD_ICON = "RECORD_ICON";
	private static final String RECORD_USERNAME = "RECORD_USERNAME";
	
	public static final String INTENT_RECORD_EDITED = "INTENT_RECORD_EDITED";
	public static final String INTENT_KEY = "INTENT_KEY";
	
	public static final int REQUEST_CODE_EDIT = 0;
	public static final int REQUEST_CODE_DETAIL = 1;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent intent = getIntent();
		records = (List<Record>) intent.getSerializableExtra(LoadingActivity.INTENT_RECORDS);
		key = intent.getByteArrayExtra(LoadingActivity.INTENT_KEY);
		recordMapList = new ArrayList<Map<String, Object>>();

		sortRecords();
		for(Record r: records){
			recordMapList.add(convertRecordsToAdapter(r));
		}
		
		adapter = new SimpleAdapter(this, recordMapList, R.layout.grid_item_main, 
				new String[] {RECORD_TAG, RECORD_USERNAME, RECORD_ICON},
				new int[] {R.id.grid_item_tag, R.id.grid_item_username, R.id.grid_item_image});
		
		GridView gridView = ((GridView)findViewById(R.id.main_grid_records));
		gridView.setAdapter(adapter);
		
		gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(context, DetailActivity.class);
				intent.putExtra(INTENT_KEY, key);
				intent.putExtra(MainActivity.INTENT_RECORD_EDITED, records.get(position));
				context.startActivityForResult(intent, MainActivity.REQUEST_CODE_DETAIL);
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
		case R.id.main_action_about:
			break;
		}
		return super.onOptionsItemSelected(item);
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
					if(!existsAndUpdateRecords(record)){
						insertIntoRecords(record);
					}
					// update gridview
					adapter.notifyDataSetChanged();
					Toast.makeText(this, R.string.main_edit_ok, Toast.LENGTH_SHORT).show();
				}
				break;
			case EditActivity.RESULT_ERROR:
				Toast.makeText(this, R.string.error_hint, Toast.LENGTH_SHORT).show();
				break;
			case EditActivity.RESULT_CANCEL:
				Toast.makeText(this, R.string.cancel_hint, Toast.LENGTH_SHORT).show();
				break;
			}
			break;
		}
	}
	
	
	
	/**
	 * convert a record to map object
	 */
	private Map<String, Object> convertRecordsToAdapter(Record r){
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
			Toast.makeText(this, R.string.error_hint, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return map;
	}
	
	private boolean existsAndUpdateRecords(Record r){
		for(int i = 0; i < records.size(); i++){
			Record record = records.get(i);
			if(r.getID() == record.getID()){
				records.set(i, r);
				recordMapList.set(i, this.convertRecordsToAdapter(r));
				sortRecords();
				sortRecordMapList();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * insert a record into both records-list and map-list (after convertion)
	 */
	private void insertIntoRecords(Record r){
		records.add(r);
		recordMapList.add(this.convertRecordsToAdapter(r));
		sortRecords();
		sortRecordMapList();
	}
	
	private void sortRecords(){
		MoonshileSort.mergeSort(new MoonshileSort.MoonshileList<List<Record>, Record>(){

			@Override
			public Record get(List<Record> list, int i) {
				return list.get(i);
			}

			@Override
			public void set(List<Record> list, Record f, int i) {
				list.set(i, f);
			}
			
		}, records, 0, records.size() - 1, new MoonshileSort.Compare<Record>(){

			@Override
			public int cmp(Record f1, Record f2) {
				int res;
				try {
					res = f1.getTag(key).compareTo(f2.getTag(key));
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

	
	private void sortRecordMapList(){
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
		}, recordMapList, 0, recordMapList.size() - 1, new MoonshileSort.Compare<Map<String, Object>>() {

			@Override
			public int cmp(Map<String, Object> f1, Map<String, Object> f2) {
				int res;
				res = ((String)f1.get(RECORD_TAG)).compareTo((String)f2.get(RECORD_TAG));
				if(res != 0){
					return res;
				}else{
					return ((String)f1.get(RECORD_USERNAME)).compareTo((String)f2.get(RECORD_USERNAME));
				}
			}
		});
	}
}
