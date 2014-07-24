package com.moonshile.failword;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.moonshile.helper.AESHelper;
import com.moonshile.helper.PRNGFixes;
import com.moonshile.storage.Record;

public class LoadingActivity extends ActionBarActivity {
	
	private static final int INIT_RECORDS_COUNT = 28;
	private SharedPreferences sharedPref;

	public static final String INTENT_KEY = "key";
	public static final String INTENT_RECORDS = "records";
	
	public static final String KEY_SET = "key has been set";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_loading);
		sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		
		// fix bugs of PRNG while using AES
		PRNGFixes.apply();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.loading, menu);
		return true;
	}
	
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
		String[] tipStrs = getResources().getStringArray(R.array.loading_tips);
		if(sharedPref.contains(KEY_SET)){
			// if the user has set global key
			tips.setText(tipStrs[new Random().nextInt(tipStrs.length)]);
		}else{
			// if this app is used first time
			tips.setText(tipStrs[0]);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onLogin(View view){
		Button button = ((Button)view);
		button.setText(R.string.loading_logining);
		button.setTextColor(getResources().getColor(R.color.gray));
		button.setClickable(false);
		Handler handler = new Handler();
		final LoadingActivity context = this;
		
		// loading data and switch activity
		handler.post(new Runnable(){
			
			private void wrongKey(){
				((EditText)findViewById(R.id.loading_password)).setText("");
				TextView tips = (TextView)findViewById(R.id.loading_tips);
				tips.setTextColor(getResources().getColor(R.color.red));
				tips.setText(R.string.loading_wrong_key_tip);

				Button button = ((Button)findViewById(R.id.loading_login));
				button.setText(R.string.loading_login);
				button.setTextColor(getResources().getColor(R.color.black));
				button.setClickable(true);
			}
			
			private void exception(){
				TextView tips = (TextView)findViewById(R.id.loading_tips);
				tips.setTextColor(getResources().getColor(R.color.red));
				tips.setText(R.string.loading_exception_tip);
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
							ArrayList<Record> records = (ArrayList<Record>)Record.fetchAllRecords(context, 0, INIT_RECORDS_COUNT, key);
							Intent intent = new Intent(context, MainActivity.class);
							intent.putExtra(INTENT_KEY, key);
							intent.putExtra(INTENT_RECORDS, records);
							context.startActivity(intent);
							context.finish();
						}
					}else{
						// if this app is used first time
						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putString(KEY_SET, AESHelper.encrypt(KEY_SET, key));
						editor.commit();
						Intent intent = new Intent(context, MainActivity.class);
						intent.putExtra(INTENT_KEY, key);
						intent.putExtra(INTENT_RECORDS, new ArrayList<Record>());
						context.startActivity(intent);
						context.finish();
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
