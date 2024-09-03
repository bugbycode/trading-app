package com.bugbycode.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
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
	
}
