package com.moonshile.helper.test;

import com.moonshile.failword.LoadingActivity;
import com.moonshile.helper.MoonshileSort;
import android.test.ActivityInstrumentationTestCase2;

public class MoonshileSortTest extends ActivityInstrumentationTestCase2<LoadingActivity> {

	public MoonshileSortTest() {
		super(LoadingActivity.class);
	}

	public void testMergeSort(){
		String[][] expect = {
				new String[]{ "1", "2", "3", "4", "5", "6" },
				new String[]{ "1", "2", "3", "4", "5", "6" },
				new String[]{ "1", "1", "2", "2", "3", "3" },
				new String[]{ "a", "b", "c", "d", "e", "f" },
				new String[]{ "a", "b", "c", "d", "e", "f" }
		};
		String[][] actual = {
				new String[]{ "6", "5", "4", "3", "2", "1" },
				new String[]{ "1", "3", "4", "2", "6", "5" },
				new String[]{ "1", "2", "3", "3", "1", "2" },
				new String[]{ "f", "e", "d", "c", "b", "a" },
				new String[]{ "a", "c", "f", "e", "b", "d" }
		};
		for(int i = 0; i < expect.length; i++){
			MoonshileSort.mergeSort(new MoonshileSort.MoonshileList<String[], String>(){

				@Override
				public String get(String[] list, int i) {
					return list[i];
				}

				@Override
				public void set(String[] list, String f, int i) {
					list[i] = f;
				}
				
			}, actual[i], 0, actual[i].length - 1, new MoonshileSort.Compare<String>(){

				@Override
				public int cmp(String f1, String f2) {
					return f1.compareTo(f2);
				}
				
			});
			
			for(int j = 0; j < expect[i].length; j++){
				assertEquals(expect[i][j], actual[i][j]);
			}
		}
	}
}
