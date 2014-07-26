package com.moonshile.failword;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.moonshile.storage.Record;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditActivity extends Activity {
	private Record record;
	private byte[] key;
	
	public static final int RESULT_OK = 0;
	public static final int RESULT_ERROR = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		
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
			Intent res = new Intent(this, MainActivity.class);
			this.setResult(RESULT_ERROR, res);
			this.finish();
		}
	}
	
	public void onSave(View view){
		Button button = (Button)view;
		button.setText(R.string.edit_saving);
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
			Intent res = new Intent(this, MainActivity.class);
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
				Intent res = new Intent(context, MainActivity.class);
				res.putExtra(MainActivity.INTENT_RECORD_EDITED, record);
				context.setResult(RESULT_OK, res);
				context.finish();
			}
			
		});
	}
}
