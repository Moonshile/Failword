package com.moonshile.failword;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.moonshile.helper.AESHelper;
import com.moonshile.storage.Record;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

public class ChangeKeyActivity extends Activity {
	
	private List<Record> records;
	
	public static final int RESULT_ERROR = 2;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_key);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		records = (List<Record>) getIntent().getSerializableExtra(MainActivity.INTENT_RECORDS);
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
		((TextView)findViewById(R.id.change_tips)).setText("");
		((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
				view.getWindowToken(), 0);
		
		final ChangeKeyActivity context = this;
		final byte[] old_key = getIntent().getByteArrayExtra(MainActivity.INTENT_KEY);
		try {
			byte[] old_key_input = AESHelper.getKeyBytes(((EditText)findViewById(R.id.change_old_pwd)).getText().toString());
			String new_key_str = ((EditText)findViewById(R.id.change_new_pwd)).getText().toString();
			String new_key_str_cfm = ((EditText)findViewById(R.id.change_new_pwd_cfm)).getText().toString();
			final byte[] new_key = AESHelper.getKeyBytes(new_key_str);
			if(AESHelper.keyAreEqual(old_key, old_key_input) && new_key_str.equals(new_key_str_cfm)){
				Handler handler = new Handler();
				handler.post(new Runnable(){

					@Override
					public void run() {
						try {
							// backup
							String path = Record.exportRecords(records);
							Record.changeKey(context, records, old_key, new_key);
							File f = new File(path);
							if(f.exists()){
								f.delete();
							}
							SharedPreferences sharedPref = context.getSharedPreferences(LoadingActivity.PREF_NAME, Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putString(LoadingActivity.KEY_SET, AESHelper.encrypt(LoadingActivity.KEY_SET, new_key));
							editor.commit();
							Intent res = context.getIntent();
							context.setResult(RESULT_OK, res);
							context.finish();
						} catch (Exception e) {
							Log.e("change password", e.toString());
							e.printStackTrace();
							Record.deleteAll(context);
							Intent res = context.getIntent();
							context.setResult(RESULT_ERROR, res);
							context.finish();
						}
						
					}
					
				});
			}else{
				if(!AESHelper.keyAreEqual(old_key, old_key_input)){
					((TextView)findViewById(R.id.change_tips)).setText(R.string.change_wrong_old_key);
				}else{
					((TextView)findViewById(R.id.change_tips)).setText(R.string.change_dif_new_keys);
				}
				button.setText(R.string.btn_save);
				button.setTextColor(getResources().getColor(R.color.dark_gray));
				button.setClickable(true);
			}
		} catch (NoSuchAlgorithmException e) {
			Toast.makeText(this, R.string.error_hint, Toast.LENGTH_SHORT).show();
			onCancel(null);
			e.printStackTrace();
		}
	}

}
