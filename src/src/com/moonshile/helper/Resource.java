
/**********************************************
 * 
 * Copyright (C) 2014  Moonshile (moonshile@foxmail.com)
 *
 **********************************************/

/**
 * @Title Resource.java
 * @Package com.moonshile.helper
 * @author Duan Kaiqiang (段凯强)
 * @date 2014-7-25
 * @update 
 *
 */
package com.moonshile.helper;

import java.lang.reflect.Field;

/**
 * @author duankq
 *
 */
public class Resource {

	/********************************** Constructor ********************************************/

	private Resource(){}
	
	/********************************** Methods ********************************************/

	public static int getDrawableResByName(Class<?> R, String name) throws IllegalAccessException, IllegalArgumentException{
		for(Class<?> c: R.getDeclaredClasses()){
			if(c.getName().equals("com.moonshile.failword.R$drawable")){
				for(Field f: c.getDeclaredFields()){
					if(f.getName().equals(name)){
						return (Integer) f.get(null);
					}
				}
			}
		}
		return -1;
	}
	
	/********************************** Fields ********************************************/

}
