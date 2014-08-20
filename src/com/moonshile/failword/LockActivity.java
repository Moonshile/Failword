package com.moonshile.failword;

import java.security.NoSuchAlgorithmException;

import com.moonshile.helper.AESHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class LockActivity extends Activity {
	
	private byte[] key;
	private int wrong = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_lock);
		
		Intent intent = getIntent();
		key = intent.getByteArrayExtra(MainActivity.INTENT_KEY);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home){
			onCancel(null);
		}
		return super.onOptionsItemSelected(item);
	}

	public void onLogin(View view){
		String keyStr = ((EditText)findViewById(R.id.lock_password)).getText().toString();
		try {
			byte[] keyInput = AESHelper.getKeyBytes(keyStr);
			if(AESHelper.keyAreEqual(key, keyInput)){
				Intent res = getIntent();
				this.setResult(RESULT_OK, res);
				this.finish();
			}else if(wrong > 0){
				Toast.makeText(this, getResources().getString(R.string.lock_wrong_key) + wrong, Toast.LENGTH_SHORT).show();
				wrong--;
				((EditText)findViewById(R.id.lock_password)).setText("");
			}else{
				onCancel(null);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.e("lock", e.toString());
			Toast.makeText(this, R.string.error_hint, Toast.LENGTH_SHORT).show();
			onCancel(null);
		}
	}
	
	public void onCancel(View view){
		Intent res = getIntent();
		this.setResult(RESULT_CANCELED, res);
		this.finish();
	}
}
