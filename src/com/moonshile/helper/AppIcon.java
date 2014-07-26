/**
 * @Title AppIcon.java
 * @Package com.moonshile.helper
 * @author Duan Kaiqiang (段凯强)
 * @date 2014-7-25
 * @update 
 *
 */
package com.moonshile.helper;

import java.util.Locale;

/**
 * @author duankq
 *
 */
public class AppIcon {

	/********************************** Constructor ********************************************/

	private AppIcon(){}
	
	/********************************** Methods ********************************************/

	/**
	 * convert names of something to its standard name
	 * @param name
	 * @return standard name
	 */
	public static String getIconName(String name){
		for(String[] string: iconNames){
			String s_name = string[0];
			for(String s: string){
				if(name.toLowerCase(Locale.US).contains(s)){
					return s_name;
				}
			}
		}
		return "app_icon_default";
	}
	
	/********************************** Fields ********************************************/

	private final static String[][] iconNames = {
			new String[]{"icbc", "工行", "工商银行", "中国工商银行"}
	};
}
