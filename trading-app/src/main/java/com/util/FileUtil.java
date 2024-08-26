package com.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Klines;

public class FileUtil {

	public static List<Klines> readKlinesFile(String pair,String path){
		return CommandUtil.format(pair, readFile(path));
	}
	
	public static void writeFile(String path,String jsonStr) {
		File file = new File(path);
		FileOutputStream fos = null;
		try {
			
			File f = new File(AppConfig.CACHE_PATH);
			if(!f.isDirectory()) {
				f.mkdirs();
			}
			
			if(!file.exists()) {
				file.createNewFile();
			}
			
			fos = new FileOutputStream(file);
			
			fos.write(jsonStr.getBytes());
			fos.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static String readFile(String path) {
		
		File file = new File(path);
		
		File f = new File(AppConfig.CACHE_PATH);
		if(!f.isDirectory()) {
			f.mkdirs();
		}
		
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuffer buffer = new StringBuffer();
		if(file.exists()) {
			try {
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis, "UTF-8");
				br = new BufferedReader(isr);
				String line = null;
				while((line = br.readLine()) != null) {
					buffer.append(line);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(br != null) {
						br.close();
					}
					if(isr != null) {
						isr.close();
					}
					if(fis != null) {
						fis.close();
					}
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buffer.toString();
	}
}