package com.util;

import java.util.Map;

public class UrlQueryStringUtil {

	public static String parse(Map<String,Object> params) {
		StringBuffer buff = new StringBuffer();
		params.forEach((k,v)->{
			if(buff.length() > 0) {
				buff.append("&");
			}
			buff.append(k + "=" + v);
		});
		return buff.toString();
	}
}
