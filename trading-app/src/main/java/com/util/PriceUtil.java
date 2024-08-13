package com.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

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
		String pattern = "0.";
		
		for(int index = 0;index < decimalPoint;index++) {
			pattern += "0";
		}
		
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		
		BigDecimal bigDecimal = new BigDecimal(decimalFormat.format(number));
		
		return bigDecimal.toPlainString();
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
	
	/**
	 * 获取日线级别斐波那契回高点K线信息
	 * 
	 * @param klinesList 所有标志性高点k线信息
	 * @param lastDayKlines 昨日k线信息
	 * @return Klines
	 */
	public static Klines getFibHightKlines(List<Klines> klinesList,Klines lastDayKlines) {
		
		int size = klinesList.size();
		Klines current = null;
		for(int index = 0;index < klinesList.size(); index++) {
			Klines next = null;//前一根k线
			current = klinesList.get(index);//当前k线
			
			//如果当前k线最高价小于昨日最高价则继续匹配下一根k线 直到匹配到比昨日最高价高的k线
			if(current.getHighPrice() < lastDayKlines.getHighPrice()) {
				continue;
			}
			
			if(index < size - 1) {
				next = klinesList.get(index + 1);//下一根k
			} else {
				continue;
			}
			
			//如果当前标志性高点大于下一根k线最高价则退出循环
			if(current.getHighPrice() > next.getHighPrice()) {
				break;
			}
		}
		
		return current;
	}
	
	/**
	 * 获取日线级别斐波那契回低点K线信息
	 * 
	 * @param klinesList 所有标志性低点k线信息
	 * @param lastDayKlines 昨日k线信息
	 * @return Klines
	 */
	public static Klines getFibLowKlines(List<Klines> klinesList,Klines lastDayKlines) {
		int size = klinesList.size();
		Klines current = null;
		for(int index = 0;index < size; index++) {
			
			Klines next = null;//前一根k线
			current = klinesList.get(index);//当前k线
			
			//如果当前k线最低价大于昨日最低价则继续匹配下一根k线 直到匹配到比昨日最低价低的k线
			if(current.getLowPrice() > lastDayKlines.getLowPrice()) {
				continue;
			}
			
			if(index < size - 1) {
				next = klinesList.get(index + 1);//下一根k
			} else {
				continue;
			}
			
			//如果当前标志性低点小于下一根k线最低价则退出循环
			if(current.getLowPrice() < next.getLowPrice()) {
				break;
			}
		}
		
		return current;
	}
	
	/**
	 * 判断做多条件
	 * @param hitPrice 开仓条件的价格
	 * @param list 最近一段时间K线信息
	 * @return
	 */
	public static boolean isLong(double hitPrice,List<Klines> list) {
		
		boolean isLong = false;
		
		if(!CollectionUtils.isEmpty(list)) {
			int size = list.size();
			Klines current = list.get(size - 1); //倒数第1根k线
			Klines parent = list.get(size - 2);//倒数第2根k线
			//LOGGER.info("parent: " + parent.toString());
			//LOGGER.info("current: " + current.toString());
			//LOGGER.info(parent.getLowPrice() <= hitPrice && parent.getClosePrice() >= hitPrice 
			//		&& current.getClosePrice() >= hitPrice);
			//上一根k线最低价小于条件价、收盘价大于条件价 且当前k线收盘价大于条件价 则适合做多
			if(parent.getLowPrice() <= hitPrice && parent.getClosePrice() >= hitPrice 
					&& current.getClosePrice() >= hitPrice) {
				isLong = true;
			}
		}
		
		return isLong;
	}
	
	/**
	 * 判断做空条件
	 * @param hitPrice 开仓条件的价格
	 * @param list 最近一段时间K线信息
	 * @return
	 */
	public static boolean isShort(double hitPrice,List<Klines> list) {
		
		boolean isShort = false;
		
		if(!CollectionUtils.isEmpty(list)) {
			int size = list.size();
			Klines current = list.get(size - 1); //当前k线
			Klines parent = list.get(size - 2);//上一根k线
			
			//上一根k线最高价大于条件价、收盘价小于条件价 且当前k线收盘价小于条件价 则适合做空
			if(parent.getHighPrice() >= hitPrice && parent.getClosePrice() <= hitPrice
					&& current.getClosePrice() <= hitPrice) {
				isShort = true;
			}
		}
		
		return isShort;
	}
	
}
