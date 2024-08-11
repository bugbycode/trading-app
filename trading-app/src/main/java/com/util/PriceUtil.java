package com.util;

import java.text.DecimalFormat;
import java.util.List;

import com.bugbycode.module.Klines;

public class PriceUtil {
	
	public static double getMaxPrice(double[] arr) {
		double result = arr[0];
		for(int index = 1;index < arr.length;index++) {
			if(result < arr[index]) {
				result = arr[index];
			}
		}
		return result;
	}
	
	public static double getMinPrice(double[] arr) {
		double result = arr[0];
		for(int index = 1;index < arr.length;index++) {
			if(result > arr[index]) {
				result = arr[index];
			}
		}
		return result;
	}
	
	public static Klines getMaxPriceKLine(List<Klines> klinesList) {
		Klines result = klinesList.get(0);
		for(int index = 1;index < klinesList.size();index++) {
			if(result.getHighPrice() < klinesList.get(index).getHighPrice()) {
				result = klinesList.get(index);
			}
		}
		return result;
	}
	
	public static Klines getMinPriceKLine(List<Klines> klinesList) {
		Klines result = klinesList.get(0);
		for(int index = 1;index < klinesList.size();index++) {
			if(result.getLowPrice() > klinesList.get(index).getLowPrice()) {
				result = klinesList.get(index);
			}
		}
		return result;
	}
	
	public static Klines getMaxPriceKLine(List<Klines> klinesList,int startIndex,int endIndex) {
		return getMaxPriceKLine(klinesList.subList(startIndex, endIndex));
	}
	
	public static Klines getMinPriceKLine(List<Klines> klinesList,int startIndex,int endIndex) {
		return getMinPriceKLine(klinesList.subList(startIndex, endIndex));
	}
	
	public static String formatDoubleDecimal(double number,int decimalPoint) {
		String pattern = "#.";
		for(int index = 0;index < decimalPoint;index++) {
			pattern += "#";
		}
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		return decimalFormat.format(number);
	}
	
	public static double getEma7(List<Klines> klinesList) {
		//周期 N
		int n = klinesList.size() - 1;
		//平滑常数
		double a = 2 / (6 + 1);
		//SMA(9)
		double sma_7 = (/*klinesList.get(0).getClosePrice() + klinesList.get(1).getClosePrice() +*/ klinesList.get(2).getClosePrice() + 
				klinesList.get(3).getClosePrice() + klinesList.get(4).getClosePrice() + klinesList.get(5).getClosePrice()
				+ klinesList.get(6).getClosePrice() + klinesList.get(7).getClosePrice() + klinesList.get(8).getClosePrice()) / 7;
		//p7
		double p_7 = klinesList.get(9).getClosePrice();
		//EMA7 
		double ema_7 = a * p_7 + (1 - a) * sma_7;
		
		return ema_7;
	}
	
	public static double getEma7_parent(List<Klines> klinesList) {
		//周期 N
		int n = klinesList.size() - 1;
		//平滑常数
		double a = 2 / (6 + 1);
		//SMA(9)
		double sma_7 = (/*klinesList.get(0).getClosePrice() +*/ klinesList.get(1).getClosePrice() + klinesList.get(2).getClosePrice() + 
				klinesList.get(3).getClosePrice() + klinesList.get(4).getClosePrice() + klinesList.get(5).getClosePrice()
				+ klinesList.get(6).getClosePrice() + klinesList.get(7).getClosePrice()) / 7;
		//p7
		double p_7 = klinesList.get(8).getClosePrice();
		//EMA7 
		double ema_7 = a * p_7 + (1 - a) * sma_7;
		
		return ema_7;
	}
	
	/**
	 * 获取涨跌幅百分比
	 * @param klinesList
	 * @param index
	 * @return
	 */
	public static double getPriceFluctuationPercentage(List<Klines> klinesList,int index) {
		
		int offset = index - 1;
		
		Klines kl = klinesList.get(index);
		
		boolean isFall = kl.isFall();
		double lowPrice = kl.getLowPrice();
		double hightPrice = kl.getHighPrice();
		
		while(offset >= 0) {
			Klines tmp = klinesList.get(offset--);
			if(isFall) {
				if(tmp.isRise()) {
					break;
				}
			} else {
				if(tmp.isFall()) {
					break;
				}
			}
			if(lowPrice > tmp.getLowPrice()) {
				lowPrice = tmp.getLowPrice();
			}
			if(hightPrice < tmp.getHighPrice()) {
				hightPrice = tmp.getHighPrice();
			}
		}
		if(kl.isFall()) {
			return ((hightPrice - lowPrice) / hightPrice) * 100;
		} else {
			return ((hightPrice - lowPrice) / lowPrice) * 100;
		}
	}
}
