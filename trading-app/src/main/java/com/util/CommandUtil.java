package com.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.bugbycode.module.Klines;

public class CommandUtil {
	public static String run(String cmd) {
		
		StringBuilder output = new StringBuilder();
		ProcessBuilder processBuilder = new ProcessBuilder();
	    processBuilder.command("bash", "-c", cmd);
	    
	    BufferedReader reader = null;
	    InputStreamReader isr = null;
	    InputStream in = null;
	    try {
            Process process = processBuilder.start();
            in = process.getInputStream();
            isr = new InputStreamReader(in);
            reader = new BufferedReader(isr);
            
            String line;
            
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            if(exitCode != 0) {
            	throw new RuntimeException("执行命令：“" + cmd + "”时出现错误");
            }
            //System.out.println("Exit Code: " + exitCode);
            //System.out.println("Output:\n" + output.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(reader != null) {
					reader.close();
				}
				if(isr != null) {
					isr.close();
				}
				if(in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return output.toString();
	}
	
	public static List<Klines> format(String pair,String str,String interval){
		int decimalNum = 0;
		List<Klines> klinesList = new ArrayList<Klines>();
		
		if(StringUtil.isNotEmpty(str)) {
			try {
				JSONArray jsonArr = new JSONArray(str);
				
				for(int index = 0;index < jsonArr.length();index++) {
					
					JSONArray klJson = jsonArr.getJSONArray(index);
					//decimalNum = klJson.getString(1).substring(klJson.getString(1).indexOf(".") + 1).length();
					decimalNum = new BigDecimal(klJson.getString(1)).scale();
					Klines kl = new Klines(pair,klJson.getLong(0),
							klJson.getString(1),klJson.getString(2),
							klJson.getString(3),klJson.getString(4),
							klJson.getLong(6),interval,decimalNum);
					klinesList.add(kl);
				};
				
			} catch (JSONException e) {
				throw new RuntimeException("解析" + pair + "返回的JSON时出现错误，错误信息：" + e.getLocalizedMessage() + "\n"
						+ "返回的JSON：" + str);
			}
		}
		return klinesList;
	}
}
