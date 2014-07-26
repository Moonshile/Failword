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
import com.moonshile.helper.Resource;
import com.moonshile.storage.Record;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	
	private List<Record> records;
	private byte[] key;
	private List<Map<String, Object>> recordMapList;
	private SimpleAdapter adapter;

	private static final String RECORD_ID = "RECORD_ID";
	private static final String RECORD_TAG = "RECORD_TAG";
	private static final String RECORD_ICON = "RECORD_ICON";
	private static final String RECORD_USERNAME = "RECORD_USERNAME";
	
	public static final String INTENT_RECORD_EDITED = "INTENT_RECORD_EDITED";
	public static final String INTENT_KEY = "INTENT_KEY";
	
	public static final int REQUEST_CODE_EDIT = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent intent = getIntent();
		records = (List<Record>) intent.getSerializableExtra(LoadingActivity.INTENT_RECORDS);
		key = intent.getByteArrayExtra(LoadingActivity.INTENT_KEY);
		recordMapList = new ArrayList<Map<String, Object>>();

		for(Record r: records){
			recordMapList.add(convertRecordsToAdapter(r));
		}
		
		adapter = new SimpleAdapter(this, recordMapList, R.layout.grid_item_main, 
				new String[] {RECORD_TAG, RECORD_USERNAME, RECORD_ICON},
				new int[] {R.id.grid_item_tag, R.id.grid_item_username, R.id.grid_item_image});
		
		((GridView)findViewById(R.id.main_grid_records)).setAdapter(adapter);
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
				Record record = (Record)intent.getSerializableExtra(INTENT_RECORD_EDITED);
				insertIntoRecords(record);
				adapter.notifyDataSetChanged();
				Toast.makeText(this, R.string.main_edit_ok, Toast.LENGTH_SHORT).show();
				break;
			case EditActivity.RESULT_ERROR:
				Toast.makeText(this, R.string.error_hint, Toast.LENGTH_SHORT).show();
				break;
			}
			break;
		}
	}
	
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
	
	/**
	 * insert a record into both records-list and map-list (after convertion)
	 */
	private void insertIntoRecords(Record r){
		int index = -1;
		for(int i = 0; i < records.size(); i++){
			Record record = records.get(i);
			try {
				if(r.getTag(key).compareTo(record.getTag(key)) < 0){
					index = i;
					break;
				}else if(r.getTag(key).compareTo(record.getTag(key)) == 0){
					if(r.getUsername(key).compareTo(record.getUsername(key)) <= 0){
						index = i;
						break;
					}
				}
			} catch (InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | NoSuchProviderException e) {
				Toast.makeText(this, R.string.error_hint, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
		records.add(r);
		recordMapList.add(this.convertRecordsToAdapter(r));
		if(index > -1){
			for(int i = records.size() - 1; i > index; i--){
				Record t = records.get(i);
				records.set(i, records.get(i - 1));
				records.set(i - 1, t);
				Map<String, Object> mt = recordMapList.get(i);
				recordMapList.set(i, recordMapList.get(i - 1));
				recordMapList.set(i - 1,  mt);
			}
		}
	}
	
	

}
