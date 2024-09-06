package com.bugbycode.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibKlinesData;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;
import com.bugbycode.service.KlinesService;
import com.util.CommandUtil;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.FileUtil;
import com.util.KlinesComparator;
import com.util.PriceUtil;
import com.util.StringUtil;

@Service("klinesService")
public class KlinesServiceImpl implements KlinesService {

	private final Logger logger = LogManager.getLogger(KlinesServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;
	
	@Override
	public List<Klines> continuousKlines(String pair, long startTime, long endTime,
			String interval,QUERY_SPLIT split) {
		
		String filePathName = AppConfig.CACHE_PATH + "/" + pair + "_" + interval + ".json";
		
		//从缓存中读取
		List<Klines> list = FileUtil.readKlinesFile(pair, filePathName);
		
		//缓存不存在或者不是最新数据
		if(list.isEmpty() || list.get(list.size() - 1).getEndTime() < endTime) {
			
			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(AppConfig.REST_BASE_URL + "/fapi/v1/continuousKlines")
	                .queryParam("pair", pair)
	                .queryParam("contractType", "PERPETUAL")
	                .queryParam("startTime", startTime)
	                .queryParam("interval", interval)
	                .queryParam("limit", 1500);
			
			switch (split) {
			case NOT_ENDTIME:
				
				uriBuilder.queryParam("endTime", endTime);
				
				break;

			default:
				break;
			}
			
			String url = uriBuilder.toUriString();
			
			logger.debug(url);
			
			String result = restTemplate.getForObject(url, String.class);
			
			list = CommandUtil.format(pair, result);
			
			FileUtil.writeFile(filePathName, result);
		} else {
			logger.debug("读取缓存文件：" + filePathName);
			logger.debug("缓存条数：" + list.size());
			logger.debug("缓存最后一条k线：" + list.get(list.size() - 1).toString());
		}
		
		return list;
	}

	@Override
	public List<Klines> continuousKlines1Day(String pair, Date now, int limit,QUERY_SPLIT split) {
		
		int hours = DateFormatUtil.getHours(now.getTime());
		Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
		Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
		
		Date firstDayStartTime = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -limit);//多少天以前起始时间
		
		return continuousKlines(pair, firstDayStartTime.getTime(), 
				lastDayEndTimeDate.getTime() + 999, Inerval.INERVAL_1D.getDescption(),split);
	}

	@Override
	public List<Klines> continuousKlines5M(String pair, Date now, int limit,QUERY_SPLIT split) {
		List<Klines> result = null;
		try {
			
			Date endTime_5m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
			Date startTime_5m = DateFormatUtil.getStartTimeBySetMinute(endTime_5m, -Inerval.INERVAL_5M.getNumber() * limit);//limit根k线
			endTime_5m = DateFormatUtil.getStartTimeBySetMillisecond(endTime_5m, -1);//收盘时间
			
			result = continuousKlines(pair, startTime_5m.getTime(),
					endTime_5m.getTime(), Inerval.INERVAL_5M.getDescption(),split);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public List<Klines> continuousKlines15M(String pair, Date now, int limit,QUERY_SPLIT split) {
		List<Klines> result = null;
		try {
			
			Date endTime = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
			Date startTime = DateFormatUtil.getStartTimeBySetMinute(endTime, -Inerval.INERVAL_15M.getNumber() * limit);//limit根k线
			endTime = DateFormatUtil.getStartTimeBySetMillisecond(endTime, -1);//收盘时间
			
			result = continuousKlines(pair, startTime.getTime(),
					endTime.getTime(), Inerval.INERVAL_15M.getDescption(),split);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public void openLong(FibInfo fibInfo, Klines afterLowKlines, List<Klines> klinesList_hit) {
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		
		List<Klines> todayKlinesList = PriceUtil.getTodayKlines(klinesList_hit);
		
		Klines hitLowKlines = CollectionUtils.isEmpty(todayKlinesList) ? null : PriceUtil.getMinPriceKLine(todayKlinesList);
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePrice();
		//double openPrice = hitKline.getOpenPrice();
		double lowPrice = hitKline.getLowPrice();
		//double hightPrice = hitKline.getHighPrice();
		double currentPrice = closePrice;
		
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//多头行情做多 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 FIB0
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];
			
			FibCode closePpositionCode = fibInfo.getTakeProfit(code);//止盈点位
			
			if(PriceUtil.isLong(fibInfo.getFibValue(code), klinesList_hit) 
					&& !PriceUtil.isObsoleteLong(fibInfo,afterLowKlines,codes,offset)
					&& !PriceUtil.isObsoleteLong(fibInfo,hitLowKlines,codes,offset)) {//FIB1~startFibCode做多

				String subject = String.format("%s永续合约%s(%s)[%s]做多机会 %s", pair, code.getDescription(),
						PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
						fibInfo.getLevel().getLabel(),
						DateFormatUtil.format(new Date()));
				
				String text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice, closePpositionCode);
			
				sendEmail(subject,text,fibInfo);

				break;
			}
			
			if(code == fibInfo.getLevel().getStartFibCode()) {
				break;
			}
			
		}
	}

	@Override
	public void openShort(FibInfo fibInfo,Klines afterHighKlines,List<Klines> klinesList_hit) {
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		
		List<Klines> todayKlinesList = PriceUtil.getTodayKlines(klinesList_hit);

		Klines hitHighKlines = CollectionUtils.isEmpty(todayKlinesList) ? null : PriceUtil.getMaxPriceKLine(todayKlinesList);
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePrice();
		//double openPrice = hitKline.getOpenPrice();
		//double lowPrice = hitKline.getLowPrice();
		double hightPrice = hitKline.getHighPrice();
		double currentPrice = closePrice;
		
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//空头行情做空 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 FIB0
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];//当前斐波那契点位

			FibCode closePpositionCode = fibInfo.getTakeProfit(code);//止盈点位
			
			if(PriceUtil.isShort(fibInfo.getFibValue(code), klinesList_hit) && 
					!PriceUtil.isObsoleteShort(fibInfo,afterHighKlines,codes,offset)
					&& !PriceUtil.isObsoleteShort(fibInfo,hitHighKlines,codes,offset)) {
				
				String subject = String.format("%s永续合约%s(%s)[%s]做空机会 %s", pair, code.getDescription(),
						PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
						fibInfo.getLevel().getLabel(),
						DateFormatUtil.format(new Date()));
				
				String text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice, closePpositionCode);
				
				sendEmail(subject,text,fibInfo);
				
				break;
			}
			
			if(code == fibInfo.getLevel().getStartFibCode()) {
				break;
			}
		}
	}
	
	private void sendEmail(String subject,String text,FibInfo fibInfo) {
		if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {

			text += "\n\n" + fibInfo.toString();
			
			logger.info("邮件主题：" + subject);
			logger.info("邮件内容：" + text);
			
			Result<ResultCode, Exception> result = EmailUtil.send(subject, text);
			
			switch (result.getResult()) {
			case ERROR:
				
				Exception ex = result.getErr();
				
				logger.info("邮件发送失败！失败原因：" + ex.getLocalizedMessage());
				
				break;
				
			default:
				
				logger.info("邮件发送成功！");
				
				break;
			}
		}
	}

	@Override
	public void sendFib0Email(FibInfo fibInfo, List<Klines> klinesList_hit) {

		String subject = "";
		String text = "";
		
		Klines lastKlines = PriceUtil.getLastKlines(klinesList_hit);
		
		double fib0Price = fibInfo.getFibValue(FibCode.FIB0);
		
		QuotationMode qm = fibInfo.getQuotationMode();
		
		switch (qm) {
		
		case LONG:
			
			if(PriceUtil.isLong(fib0Price, klinesList_hit)) {
				
				subject = String.format("%s永续合约突破%s(%s)[%s] %s", lastKlines.getPair(), FibCode.FIB0.getDescription(),
						PriceUtil.formatDoubleDecimal(fib0Price,fibInfo.getDecimalPoint()), fibInfo.getLevel().getLabel(), DateFormatUtil.format(new Date()));
				
			} else if(PriceUtil.isShort(fib0Price, klinesList_hit)) {
				
				subject = String.format("%s永续合约突破%s(%s)[%s]并收回 %s", lastKlines.getPair(), FibCode.FIB0.getDescription(),
						PriceUtil.formatDoubleDecimal(fib0Price,fibInfo.getDecimalPoint()), fibInfo.getLevel().getLabel(), DateFormatUtil.format(new Date()));
				
			}
			
			break;

		default:
			
			if(PriceUtil.isLong(fib0Price, klinesList_hit)) {
				
				subject = String.format("%s永续合约跌破%s(%s)[%s]并收回 %s", lastKlines.getPair(), FibCode.FIB0.getDescription(),
						PriceUtil.formatDoubleDecimal(fib0Price,fibInfo.getDecimalPoint()), fibInfo.getLevel().getLabel(), DateFormatUtil.format(new Date()));
				
			} else if(PriceUtil.isShort(fib0Price, klinesList_hit)) {
				
				subject = String.format("%s永续合约跌破%s(%s)[%s] %s", lastKlines.getPair(), FibCode.FIB0.getDescription(),
						PriceUtil.formatDoubleDecimal(fib0Price,fibInfo.getDecimalPoint()), fibInfo.getLevel().getLabel(), DateFormatUtil.format(new Date()));
				
			}
			
			break;
			
		}
		
		if(StringUtil.isNotEmpty(subject)) {
			sendEmail(subject, text, fibInfo);
		}
	}

	@Override
	public void futuresHighOrLowMonitor(List<Klines> klinesList,List<Klines> klinesList_hit) {
		KlinesComparator kc = new KlinesComparator();
		FibKlinesData<List<Klines>,List<Klines>> fibKlinesData = PriceUtil.getFibKlinesData(klinesList);
		
		//标志性高点K线信息
		List<Klines> lconicHighPriceList = fibKlinesData.getLconicHighPriceList();
		//标志性低点K线信息
		List<Klines> lconicLowPriceList = fibKlinesData.getLconicLowPriceList();
		
		//昨日K线信息
		Klines lastDayKlines = PriceUtil.getLastKlines(klinesList);
		
		String pair = lastDayKlines.getPair();
		
		lconicHighPriceList.add(lastDayKlines);
		lconicLowPriceList.add(lastDayKlines);
		
		//排序 按开盘时间升序 从旧到新
		lconicHighPriceList.sort(kc);
		lconicLowPriceList.sort(kc);
		
		
		Klines hitLowKlines = PriceUtil.getPositionLowKlines(lconicLowPriceList, klinesList_hit);
		Klines hitHighKlines = PriceUtil.getPositionHighKlines(lconicHighPriceList, klinesList_hit);
		
		String dateStr = DateFormatUtil.format(new Date());
		
		String subject = "";
		String text = "";
		
		String lastDayStr = "";
		
		if(!ObjectUtils.isEmpty(hitLowKlines)) {
			
			double lowPrice = hitLowKlines.getLowPrice();
			
			if(lastDayKlines.isEquals(hitLowKlines)) {
				//lastDayStr = "昨日最低价";
				return;
			}
			
			if(PriceUtil.isLong(lowPrice, klinesList_hit)) {
				
				subject = String.format("%s永续合约跌破%s(%s)并收回 %s", pair,lastDayStr,PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()),dateStr);
				
				text = String.format("%s永续合约跌破(%s)最低价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(hitLowKlines.getStarTime())), 
						PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()));
			} else if(PriceUtil.isShort(lowPrice, klinesList_hit)) {
				
				subject = String.format("%s永续合约跌破%s(%s) %s", pair,lastDayStr,PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()),dateStr);
				
				text = String.format("%s永续合约跌破(%s)最低价(%s)", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(hitLowKlines.getStarTime())), 
						PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()));
			}
		
		} else if(!ObjectUtils.isEmpty(hitHighKlines)) {
			
			if(lastDayKlines.isEquals(hitHighKlines)) {
				//lastDayStr = "昨日最高价";
				return;
			}
			
			double highPrice = hitHighKlines.getHighPrice();
			
			if(PriceUtil.isLong(highPrice, klinesList_hit)) {
				
				subject = String.format("%s永续合约突破%s(%s) %s", pair,lastDayStr,PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()),dateStr);
				
				text = String.format("%s永续合约突破(%s)最高价(%s)", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(hitHighKlines.getStarTime())), 
						PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()));
				
			} else if(PriceUtil.isShort(highPrice, klinesList_hit)) {
				
				subject = String.format("%s永续合约突破%s(%s)并收回 %s", pair,lastDayStr,PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()),dateStr);
				
				text = String.format("%s永续合约突破(%s)最高价(%s)并收回", pair,
						DateFormatUtil.format_yyyy_mm_dd(new Date(hitHighKlines.getStarTime())), 
						PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()));
				
			}
			
		}
		
		if(StringUtil.isNotEmpty(subject)) {
			
			text += "\n\n" + dateStr;
			
			logger.info("邮件主题：" + subject);
			logger.info("邮件内容：" + text);
			
			Result<ResultCode, Exception> result = EmailUtil.send(subject, text);
			
			switch (result.getResult()) {
			case ERROR:
				
				Exception ex = result.getErr();
				
				logger.info("邮件发送失败！失败原因：" + ex.getLocalizedMessage());
				
				break;
				
			default:
				
				logger.info("邮件发送成功！");
				
				break;
			}
		}
	}

	@Override
	public void futuresFibMonitor(List<Klines> klinesList, List<Klines> klinesList_hit) {
		//昨日K线信息
		Klines lastDayKlines = PriceUtil.getLastKlines(klinesList);
		
		String pair = lastDayKlines.getPair();
		
		FibKlinesData<List<Klines>,List<Klines>> fibKlinesData = PriceUtil.getFibKlinesData(klinesList);
		
		//标志性高点K线信息
		List<Klines> lconicHighPriceList = fibKlinesData.getLconicHighPriceList();
		//标志性低点K线信息
		List<Klines> lconicLowPriceList = fibKlinesData.getLconicLowPriceList();
		
		//获取斐波那契回撤高点
		Klines fibHightKlines = PriceUtil.getFibHightKlines(lconicHighPriceList,lastDayKlines);
		//获取斐波那契回撤低点
		Klines fibLowKlines = PriceUtil.getFibLowKlines(lconicLowPriceList,lastDayKlines);
		
		if(ObjectUtils.isEmpty(fibHightKlines) || ObjectUtils.isEmpty(fibLowKlines)) {
			logger.debug("无法计算出" + pair + "第一级别斐波那契回撤信息");
			return;
		}
		
		//修正斐波那契回撤点位
		fibHightKlines = PriceUtil.rectificationFibHightKlines(lconicHighPriceList, fibLowKlines, fibHightKlines);
		fibLowKlines = PriceUtil.rectificationFibLowKlines(lconicLowPriceList, fibLowKlines, fibHightKlines);
		
		//第一级别斐波那契
		//斐波那契回撤信息
		FibInfo fibInfo = new FibInfo(fibLowKlines, fibHightKlines, fibLowKlines.getDecimalNum(),FibLevel.LEVEL_1);
		
		Klines afterHighKlines = null;//回撤之后的最高日线
		Klines afterLowKlines = null;//回撤之后最低日线
		
		//第一级别趋势
		QuotationMode qm = fibInfo.getQuotationMode();
		//第一级别斐波那契开仓
		switch (qm) {
		case LONG:
			afterLowKlines = PriceUtil.getLowKlinesByStartKlines(klinesList, fibHightKlines);
			openLong(fibInfo, afterLowKlines, klinesList_hit);
			break;

		default:
			afterHighKlines = PriceUtil.getHightKlinesByStartKlines(klinesList, fibLowKlines);
			openShort(fibInfo,afterHighKlines,klinesList_hit);
			break;
		}
		
		//开始获取第二级别斐波那契回撤信息
		Klines secondFibHightKlines = null;
		Klines secondFibLowKlines = null;
		
		//开始获取第二级别斐波那契回撤终点
		switch (qm) {
		case LONG:
			secondFibHightKlines = fibHightKlines;
			secondFibLowKlines = PriceUtil.getLowKlinesByStartKlines(klinesList, secondFibHightKlines);
			break;

		default:
			secondFibLowKlines = fibLowKlines;
			secondFibHightKlines = PriceUtil.getHightKlinesByStartKlines(klinesList, secondFibLowKlines);
			break;
		}
		
		if(ObjectUtils.isEmpty(secondFibHightKlines) || ObjectUtils.isEmpty(secondFibLowKlines)) {
			logger.debug("无法计算出" + pair + "第二级别斐波那契回撤信息");
			
			sendFib0Email(fibInfo, klinesList_hit);
			
			return;
		}
		
		FibInfo secondFibInfo = new FibInfo(secondFibLowKlines, secondFibHightKlines, secondFibLowKlines.getDecimalNum(),FibLevel.LEVEL_2);
		
		//第二级别趋势
		QuotationMode secondQm = secondFibInfo.getQuotationMode();
		
		//第二级别斐波那契开仓
		switch (secondQm) {
		case LONG:
			afterLowKlines = PriceUtil.getLowKlinesByStartKlines(klinesList, secondFibHightKlines);
			openLong(secondFibInfo,afterLowKlines, klinesList_hit);
			break;

		default:
			afterHighKlines = PriceUtil.getHightKlinesByStartKlines(klinesList, secondFibLowKlines);
			openShort(secondFibInfo, afterHighKlines, klinesList_hit);
			break;
		}
		
		//开始获取第三级别斐波那契回撤信息
		Klines thirdFibHightKlines = null;
		Klines thirdFibLowKlines = null;
		//开始获取第三级斐波那契回撤
		switch (secondQm) {
		case LONG:
			thirdFibHightKlines = secondFibHightKlines;
			thirdFibLowKlines = PriceUtil.getLowKlinesByStartKlines(klinesList, thirdFibHightKlines);
			break;

		default:
			thirdFibLowKlines = secondFibLowKlines;
			thirdFibHightKlines = PriceUtil.getHightKlinesByStartKlines(klinesList, thirdFibLowKlines);
			break;
		}
		
		if(ObjectUtils.isEmpty(thirdFibHightKlines) || ObjectUtils.isEmpty(thirdFibLowKlines)) {
			logger.debug("无法计算出" + pair + "第三级别斐波那契回撤信息");
			
			sendFib0Email(secondFibInfo, klinesList_hit);
			
			return;
		}
		
		FibInfo thirdFibInfo = new FibInfo(thirdFibLowKlines, thirdFibHightKlines, thirdFibLowKlines.getDecimalNum(),FibLevel.LEVEL_3);
		
		QuotationMode thirdQm = thirdFibInfo.getQuotationMode();
		
		//第三级别斐波那契开仓
		switch (thirdQm) {
		case LONG:
			afterLowKlines = PriceUtil.getLowKlinesByStartKlines(klinesList, thirdFibHightKlines);
			openLong(thirdFibInfo, afterLowKlines,klinesList_hit);
			break;

		default:
			afterHighKlines = PriceUtil.getHightKlinesByStartKlines(klinesList, thirdFibLowKlines);
			openShort(thirdFibInfo,afterHighKlines, klinesList_hit);
			break;
		}
		
		sendFib0Email(thirdFibInfo, klinesList_hit);
	}
	
}
