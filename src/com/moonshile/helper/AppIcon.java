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
					return path + s_name;
				}
			}
		}
		return path + "app_icon_default";
	}
	
	public static String getStandardName(String name){
		String iconName = getIconName(name);
		if(iconName.equals("app_icon_default")){
			return name;
		}
		return iconName;
	}
	
	/********************************** Fields ********************************************/
	
	public static final String path = "";

	private final static String[][] iconNames = {
			new String[]{"icbc", "工行", "工商银行", "中国工商银行"},
			new String[]{"abc", "农行", "农业银行", "中国农业银行"},
			new String[]{"boc", "中行", "中国银行"},
			new String[]{"bocom", "交行", "交通银行"},
			new String[]{"ccb", "建行", "建设银行", "中国建设银行"},
			new String[]{"cebbank", "光大", "光大银行"},
			new String[]{"cib", "兴业", "兴业银行"},
			new String[]{"cmb", "招行", "招商银行"},
			new String[]{"hxb", "华夏银行"},
			new String[]{"icbc", "工行", "工商银行", "中国工商银行"},
			new String[]{"spdb", "浦发", "浦发银行", "上海浦发", "上海浦发银行", "上海浦东发展银行"},
			new String[]{"baidu", "百度"},
			new String[]{"google", "谷歌", "gmail", "g+"},
			new String[]{"qq", "q", "tx", "腾讯", "企鹅"},
			new String[]{"taobao", "淘宝", "阿里巴巴", "tmall", "tianmao", "天猫"},
			new String[]{"zhifubao", "支付宝"},
			new String[]{"jd", "京东"},
			new String[]{"meituan", "美团"},
			new String[]{"yhd", "1hd", "1号店", "一号店"},
			new String[]{"youku", "优酷"}
			
	};
}
