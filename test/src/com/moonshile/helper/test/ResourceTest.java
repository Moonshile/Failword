package com.moonshile.helper.test;

import com.moonshile.failword.MainActivity;
import com.moonshile.failword.R;
import com.moonshile.helper.Resource;

import android.test.ActivityInstrumentationTestCase2;

public class ResourceTest extends ActivityInstrumentationTestCase2<MainActivity> {
	
	public ResourceTest() {
		super(MainActivity.class);
	}

	public void testGetDrawableResByName() throws IllegalAccessException, IllegalArgumentException{
		int expect = 0x7f020057;
		int actual = Resource.getDrawableResByName(R.class, "add");
		assertEquals(expect, actual);
	}
}
