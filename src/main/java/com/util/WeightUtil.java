package com.util;

public class WeightUtil {

	public static void verifyWeight(int weight) {
		long millis = (60 * 1000) / 2400 * weight;
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
