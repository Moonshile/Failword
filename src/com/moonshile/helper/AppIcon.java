/**
 * @Title AppIcon.java
 * @Package com.moonshile.helper
 * @author Duan Kaiqiang (段凯强)
 * @date 2014-7-25
 * @update 
 *
 */
package com.moonshile.helper;

import java.util.ArrayList;
import java.util.List;
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
		List<String[]> candidates = new ArrayList<String[]>();
		for(String[] string: iconNames){
			for(String s: string){
				if(name.toLowerCase(Locale.US).contains(s)){
					candidates.add(string);
					break;
				}
			}
		}
		if(candidates.size() == 1){
			return PATH + candidates.get(0)[0];
		}else if(candidates.size() > 1){
			for(String[] string: candidates){
				for(String s: string){
					if(name.toLowerCase(Locale.US).equals(s)){
						return PATH + string[0];
					}
				}
			}
		}
		return PATH + DEFAULT_ICON;
	}
	
	public static String getStandardName(String name){
		String iconName = getIconName(name);
		if(iconName.equals(PATH + DEFAULT_ICON)){
			return name;
		}
		return iconName;
	}
	
	/********************************** Fields ********************************************/
	
	public static final String PATH = "";
	public static final String DEFAULT_ICON = "app_icon_default";

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
			new String[]{"taobao", "淘宝", "阿里", "tmall", "tianmao", "天猫"},
			new String[]{"zhifubao", "支付宝"},
			new String[]{"jd", "京东"},
			new String[]{"meituan", "美团"},
			new String[]{"yhd", "1hd", "1号店", "一号店"},
			new String[]{"youku", "优酷"},
			new String[]{"_11", "11", "11对战平台", "妖妖", "妖妖对战平台", "5211", "5211game"},
			new String[]{"_115", "115", "115网盘", "互联我"},
			new String[]{"_12306", "12306", "火车票", "中国铁路客户服务中心"},
			new String[]{"amazon", "z.cn", "亚马逊"},
			new String[]{"github", "git"},
			new String[]{"uuu9", "u9", "游久"},
			new String[]{"dropbox"},
			new String[]{"foxmail"},
			new String[]{"renren", "人人"},
			new String[]{"huawei", "华为"},
			new String[]{"cnblogs", "bokeyuan", "博客园"},
			new String[]{"xuexin", "chsi", "学信"},
			new String[]{"mi", "xiaomi", "小米", "红米"},
			new String[]{"shanbay", "shanbei", "扇贝"},
			new String[]{"sina", "xinlang", "新浪"},
			new String[]{"wangyi", "netease", "126", "163", "网易", "yeah"},
			new String[]{"jinshan", "金山", "猎豹", "liebao", "kingsoft"},
			new String[]{""},
			
	};
}
