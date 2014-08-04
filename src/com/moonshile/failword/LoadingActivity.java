package com.moonshile.failword;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.moonshile.helper.AESHelper;
import com.moonshile.helper.PRNGFixes;
import com.moonshile.storage.Record;

public class LoadingActivity extends Activity {
	
	//private static final int INIT_RECORDS_COUNT = 28;
	private SharedPreferences sharedPref;
	private String import_path;

	public static final String INTENT_KEY = "key";
	public static final String INTENT_RECORDS = "records";
	public static final String INTENT_IMPORT_PATH = "import path";
	public static final String PREF_NAME = "moonpref";
	
	public static final String KEY_SET = "key has been set";
	public static final String LAST_SIGNING = "last signing";
	public static final String ERROR_COUNT = "error count";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_loading);
		sharedPref = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		
		Intent intent = getIntent();
		Uri uri = intent.getData();
		if(uri != null){
			import_path = uri.toString().replace(uri.getScheme() + "://", "");
		}
		
		// fix bugs of PRNG while using AES
		PRNGFixes.apply();
	}
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onResume(){
		super.onResume();
		// reset performance
		((TextView)findViewById(R.id.loading_text)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/SNAP.TTF"));
		((EditText)findViewById(R.id.loading_password)).setText("");
		Button button = ((Button)findViewById(R.id.loading_login));
		button.setText(R.string.loading_login);
		button.setTextColor(getResources().getColor(R.color.black));
		button.setClickable(true);
		TextView tips = (TextView)findViewById(R.id.loading_tips);
		tips.setTextColor(getResources().getColor(R.color.dark_gray));
		String[] tipStrs = getResources().getStringArray(R.array.loading_tips);
		if(sharedPref.contains(KEY_SET)){
			// if the user has set global key
			tips.setText(tipStrs[new Random().nextInt(tipStrs.length)]);
		}else{
			// if this app is used first time
			tips.setText(tipStrs[0]);
		}
		String last_signing = sharedPref.getString(LAST_SIGNING, null);
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		if(last_signing == null || last_signing.compareTo(today) < 0){
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(LAST_SIGNING, today);
			editor.putInt(ERROR_COUNT, 0);
			editor.commit();
		}
		int error_count = sharedPref.getInt(ERROR_COUNT, 0);
		if(error_count >= 3){
			button.setClickable(false);
			button.setTextColor(getResources().getColor(R.color.gray));
			tips.setTextColor(getResources().getColor(R.color.light_red));
			tips.setText(R.string.loading_forbidden);
		}
	}
	
	public void onLogin(View view){
		Button button = ((Button)view);
		button.setText(R.string.loading_logining);
		button.setTextColor(getResources().getColor(R.color.gray));
		button.setClickable(false);
		((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
				view.getWindowToken(), 0);
		
		Handler handler = new Handler();
		final LoadingActivity context = this;
		
		// loading data and switch activity
		handler.post(new Runnable(){
			
			private void wrongKey(){
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putInt(ERROR_COUNT, sharedPref.getInt(ERROR_COUNT, 0) + 1);
				editor.commit();
				
				((EditText)findViewById(R.id.loading_password)).setText("");
				TextView tips = (TextView)findViewById(R.id.loading_tips);
				tips.setTextColor(getResources().getColor(R.color.light_red));
				tips.setText(sharedPref.getInt(ERROR_COUNT, 0) >= 3 ? 
						R.string.loading_forbidden : R.string.loading_wrong_key_tip);

				Button button = ((Button)findViewById(R.id.loading_login));
				button.setText(R.string.loading_login);
				button.setTextColor(getResources().getColor(sharedPref.getInt(ERROR_COUNT, 0) >= 3 ? 
						R.color.gray : R.color.black));
				button.setClickable(true);
			}
			
			private void exception(){
				TextView tips = (TextView)findViewById(R.id.loading_tips);
				tips.setTextColor(getResources().getColor(R.color.light_red));
				tips.setText(R.string.loading_exception_tip);
			}
			
			private void switchActivity(ArrayList<Record> records, byte[] key){
				Intent intent = new Intent(context, MainActivity.class);
				intent.putExtra(INTENT_KEY, key);
				intent.putExtra(INTENT_RECORDS, records);
				intent.putExtra(INTENT_IMPORT_PATH, import_path);
				context.startActivity(intent);
				context.finish();
			}

			@Override
			public void run() {
				String password = ((EditText)findViewById(R.id.loading_password)).getText().toString();
				try {
					byte[] key = AESHelper.getKeyBytes(password);
					
					if(sharedPref.contains(KEY_SET)){
						// if the user has set global key
						String flag = sharedPref.getString(KEY_SET, null);
						if(!KEY_SET.equals(AESHelper.decrypt(flag, key))){
							// key is wrong
							wrongKey();
						}else{
							ArrayList<Record> records = (ArrayList<Record>)Record.fetchAllRecords(context, 0, -1, key);
							switchActivity(records, key);
						}
					}else{
						// if this app is used first time
						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putString(KEY_SET, AESHelper.encrypt(KEY_SET, key));
						editor.commit();
						switchActivity(new ArrayList<Record>(), key);
					}
				} catch (InvalidKeyException | NoSuchAlgorithmException
						| NoSuchPaddingException | IllegalBlockSizeException
						| NoSuchProviderException | IllegalArgumentException e) {
					e.printStackTrace();
					exception();
				} catch(BadPaddingException e){
					if(e.toString().contains("pad block corrupted")){
						wrongKey();
					}else{
						e.printStackTrace();
						exception();
					}
				}
			}
			
		});
	}
}
