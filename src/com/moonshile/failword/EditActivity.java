package com.moonshile.failword;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.moonshile.storage.Record;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditActivity extends Activity {
	private Record record;
	private byte[] key;
	
	public static final int RESULT_ERROR = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		record = (Record)intent.getSerializableExtra(MainActivity.INTENT_RECORD_EDITED);
		key = intent.getByteArrayExtra(MainActivity.INTENT_KEY);
		try {
			if(record != null){
					((EditText)findViewById(R.id.record_edit_tag)).setText(record.getTag(key));
					((EditText)findViewById(R.id.record_edit_username)).setText(record.getUsername(key));
					((EditText)findViewById(R.id.record_edit_password)).setText(record.getPassword(key));
					((EditText)findViewById(R.id.record_edit_remarks)).setText(record.getRemarks(key));
			}else{
				record = new Record("", "", "", "", key);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | NoSuchProviderException e) {
			e.printStackTrace();
			Intent res = getIntent();
			this.setResult(RESULT_ERROR, res);
			this.finish();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home){
			onCancel(null);
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onCancel(View view){
		Intent res = getIntent();
		this.setResult(RESULT_CANCELED, res);
		this.finish();
	}
	
	public void onSave(View view){
		Button button = (Button)view;
		button.setText(R.string.btn_saving);
		button.setTextColor(getResources().getColor(R.color.gray));
		button.setClickable(false);
		try{
			record.setTag(((EditText)findViewById(R.id.record_edit_tag)).getText().toString(), key);
			record.setUsername(((EditText)findViewById(R.id.record_edit_username)).getText().toString(), key);
			record.setPassword(((EditText)findViewById(R.id.record_edit_password)).getText().toString(), key);
			record.setRemarks(((EditText)findViewById(R.id.record_edit_remarks)).getText().toString(), key);
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | NoSuchProviderException e) {
			e.printStackTrace();
			Intent res = getIntent();
			this.setResult(RESULT_ERROR, res);
			this.finish();
		}
		final Activity context = this;
		Handler handler = new Handler();
		handler.post(new Runnable(){

			@Override
			public void run() {
				if(record.getID() == Record.NOT_ADDED){
					record.add(context);
				}else{
					record.update(context);
				}
				Intent res = context.getIntent();
				res.putExtra(MainActivity.INTENT_RECORD_EDITED, record);
				context.setResult(RESULT_OK, res);
				context.finish();
			}
			
		});
	}
}
