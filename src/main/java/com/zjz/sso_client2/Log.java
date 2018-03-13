package com.zjz.sso_client2;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	public static boolean show=true;
	public static void print(String msg) {
		if(!show) {
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
	    String dateString = sdf.format(new Date());
		System.out.println(dateString+"---"+msg);
	}
	public static void main(String[] args) {
		print("aaaaaaa");
	}

}
