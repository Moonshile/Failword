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
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.*;

public class DetailActivity extends Activity {
	
	public static final int RESULT_DELETED = 2;
	public static final int RESULT_ERROR = 3;

	public static final int REQUEST_CODE_EDIT = 0;
	
	private byte[] key;
	private Record record;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_detail);
		
		Intent intent = getIntent();
		key = intent.getByteArrayExtra(MainActivity.INTENT_KEY);
		record = (Record)intent.getSerializableExtra(MainActivity.INTENT_RECORD_EDITED);
		
		setTextView();

		final DetailActivity context = this;
		((Button)findViewById(R.id.detail_delete)).setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View view) {
				Handler handler = new Handler();
				handler.post(new Runnable(){

					@Override
					public void run() {
						record.delete(context);
						Intent res = context.getIntent();
						res.putExtra(MainActivity.INTENT_RECORD_EDITED, record);
						context.setResult(RESULT_DELETED, res);
						context.finish();
					}
					
				});
				return true;
			}
			
		});
	}
	
	public void onBack(View view){
		Intent res = getIntent();
		res.putExtra(MainActivity.INTENT_RECORD_EDITED, record);
		this.setResult(RESULT_OK, res);
		this.finish();
	}
	
	public void onDeleteWarning(View view){
		Toast.makeText(this, R.string.detail_delete_warning, Toast.LENGTH_SHORT).show();
	}
	
	public void onEdit(View view){
		Intent intent = new Intent(this, EditActivity.class);
		intent.putExtra(MainActivity.INTENT_KEY, key);
		intent.putExtra(MainActivity.INTENT_RECORD_EDITED, record);
		this.startActivityForResult(intent, REQUEST_CODE_EDIT);
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
					record = (Record)intent.getSerializableExtra(MainActivity.INTENT_RECORD_EDITED);
					setTextView();
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
		}
	}
	
	private void setTextView(){
		try {
			((TextView)findViewById(R.id.detail_tag_content)).setText(record.getTag(key));
			((TextView)findViewById(R.id.detail_username_content)).setText(record.getUsername(key));
			((TextView)findViewById(R.id.detail_password_content)).setText(record.getPassword(key));
			((TextView)findViewById(R.id.detail_remarks_content)).setText(record.getRemarks(key));
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | NoSuchProviderException e) {
			e.printStackTrace();
			Intent res = getIntent();
			this.setResult(RESULT_ERROR, res);
			this.finish();
		}
	}
}