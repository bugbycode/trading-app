package com.bugbycode.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;
import com.bugbycode.service.KlinesService;
import com.util.CommandUtil;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

@Service("klinesService")
public class KlinesServiceImpl implements KlinesService {

	private final Logger logger = LogManager.getLogger(KlinesServiceImpl.class);

	@Override
	public List<Klines> continuousKlines(String pair, long startTime, long endTime,
			String interval) {
		String command = String.format("curl -G -d 'pair=%s&contractType=PERPETUAL&startTime=%s&endTime=%s"
				+ "&interval=%s' %s/fapi/v1/continuousKlines", pair, startTime, endTime, interval, AppConfig.REST_BASE_URL);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines1Day(String pair, Date now, int limit) {
		
		int hours = DateFormatUtil.getHours(now.getTime());
		Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
		Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
		
		Date firstDayStartTime = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -limit);//多少天以前起始时间
		
		return continuousKlines(pair, firstDayStartTime.getTime(), 
				lastDayEndTimeDate.getTime(), Inerval.INERVAL_1D.getDescption());
	}

	@Override
	public List<Klines> continuousKlines5M(String pair, Date now, int limit) {
		List<Klines> result = null;
		try {
			
			Date endTime_5m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
			Date startTime_5m = DateFormatUtil.getStartTimeBySetMinute(endTime_5m, -Inerval.INERVAL_5M.getNumber() * limit);//limit根k线
			endTime_5m = DateFormatUtil.getStartTimeBySetSecond(endTime_5m, -1);//收盘时间
			
			result = continuousKlines(pair, startTime_5m.getTime(),
					endTime_5m.getTime(), Inerval.INERVAL_5M.getDescption());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public List<Klines> continuousKlines15M(String pair, Date now, int limit) {
		List<Klines> result = null;
		try {
			
			Date endTime = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
			Date startTime = DateFormatUtil.getStartTimeBySetMinute(endTime, -Inerval.INERVAL_15M.getNumber() * limit);//limit根k线
			endTime = DateFormatUtil.getStartTimeBySetSecond(endTime, -1);//收盘时间
			
			result = continuousKlines(pair, startTime.getTime(),
					endTime.getTime(), Inerval.INERVAL_15M.getDescption());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public void openLong(FibInfo fibInfo, FibCode startFibCode, List<Klines> klinesList_hit) {
		
		Klines hitKline = klinesList_hit.get(klinesList_hit.size() - 1);
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePrice();
		//double openPrice = hitKline.getOpenPrice();
		double lowPrice = hitKline.getLowPrice();
		//double hightPrice = hitKline.getHighPrice();
		double currentPrice = closePrice;
		
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//多头行情做多 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236
		for(int offset = codes.length - 1;offset > 0;offset--) {
			
			FibCode code = codes[offset];
			
			FibCode closePpositionCode = null;
			
			switch (code) {
			case FIB66:
				closePpositionCode = codes[offset - 2];
				break;
			case FIB786:
				closePpositionCode = codes[offset - 2];
				break;
			default:
				
				closePpositionCode = codes[offset - 1];
				
				break;
			}
			
			if(PriceUtil.isLong(fibInfo.getFibValue(code), klinesList_hit)) {//FIB1~startFibCode做多

				String subject = String.format("%s永续合约%s(%s)[%s]做多机会 %s", pair, code.getDescription(),
						PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
						fibInfo.getLevel().getLabel(),
						DateFormatUtil.format(new Date()));
				
				String text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice, closePpositionCode);
			
				sendEmail(subject,text,fibInfo);

				break;
			}
			
			if(code.getValue() == startFibCode.getValue()) {
				break;
			}
			
		}
	}

	@Override
	public void openShort(FibInfo fibInfo, FibCode startFibCode, List<Klines> klinesList_hit) {
		
		Klines hitKline = klinesList_hit.get(klinesList_hit.size() - 1);
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePrice();
		//double openPrice = hitKline.getOpenPrice();
		//double lowPrice = hitKline.getLowPrice();
		double hightPrice = hitKline.getHighPrice();
		double currentPrice = closePrice;
		
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//空头行情做空 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 
		for(int offset = codes.length - 1;offset > 0;offset--) {
			
			FibCode code = codes[offset];//当前斐波那契点位

			FibCode closePpositionCode = null;
			
			switch (code) {
			
			case FIB66:
				closePpositionCode = codes[offset - 2];
				break;
			case FIB786:
				closePpositionCode = codes[offset - 2];
				break;
			default:
				
				closePpositionCode = codes[offset - 1];
				
				break;
			}
			
			if(PriceUtil.isShort(fibInfo.getFibValue(code), klinesList_hit)) {
				
				String subject = String.format("%s永续合约%s(%s)[%s]做空机会 %s", pair, code.getDescription(),
						PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
						fibInfo.getLevel().getLabel(),
						DateFormatUtil.format(new Date()));
				
				String text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice, closePpositionCode);
				
				sendEmail(subject,text,fibInfo);
				
				break;
			}
			
			if(code.getValue() == startFibCode.getValue()) {
				break;
			}
		}
	}
	
	private void sendEmail(String subject,String text,FibInfo fibInfo) {
		if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {

			text += "\n\n" + "[" + fibInfo.getLevel().getLabel() + "]" + fibInfo.getQuotationMode().getLabel() + "：" + fibInfo.toString();
			
			logger.info("邮件主题：" + subject);
			logger.info("邮件内容：" + text);
			
			Result<ResultCode, Exception> result = EmailUtil.send(subject, text);
			
			switch (result.getResult()) {
			case ERROR:
				
				Exception ex = result.getErr();
				
				logger.info("邮件发送失败！失败原因：" + ex.getLocalizedMessage());
				
				ex.printStackTrace();
				
				break;
				
			default:
				
				logger.info("邮件发送成功！");
				
				break;
			}
		}
	}

}
