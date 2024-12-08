package com.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import com.coinkline.module.Method;

public class MethodDataUtil {

	public static JSONObject getMethodJsonObjec(Method method) {
		JSONObject methodJson = new JSONObject();
		methodJson.put("id", RandomUtil.GetGuid32());
		methodJson.put("method", method.value());
		return methodJson;
	}
	
	/**
	 * 对数据签名
	 * @param data
	 * @param secretKey
	 */
	public static void generateSignature(JSONObject data,String secretKey) {
		List<String> keyList = new ArrayList<String>(data.keySet());
		Collections.sort(keyList);
		
		StringBuffer buff = new StringBuffer();
		for(String key : keyList) {
			if(buff.length() > 0) {
				buff.append("&");
			}
			buff.append(key + "=" + data.get(key));
		}
		String signature = HmacSHA256Util.generateSignature(buff.toString(), secretKey);
		data.put("signature", signature);
	}
}
