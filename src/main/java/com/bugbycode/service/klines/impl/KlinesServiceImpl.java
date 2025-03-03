package com.bugbycode.service.klines.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bugbycode.binance.trade.rest.BinanceRestTradeService;
import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.LongOrShortType;
import com.bugbycode.module.MonitorStatus;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.RecvTradeStatus;
import com.bugbycode.module.ResultCode;
import com.bugbycode.module.ShapeInfo;
import com.bugbycode.module.SortType;
import com.bugbycode.module.TradeStepBackStatus;
import com.bugbycode.module.TradeStyle;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.DrawTrade;
import com.bugbycode.module.binance.MarginType;
import com.bugbycode.module.binance.PriceInfo;
import com.bugbycode.module.binance.Result;
import com.bugbycode.module.binance.SymbolConfig;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.email.EmailRepository;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.repository.shape.ShapeRepository;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.service.user.UserService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.email.SendMailTask;
import com.util.CommandUtil;
import com.util.DateFormatUtil;
import com.util.FibUtil;
import com.util.FibUtil_v2;
import com.util.FibUtil_v3;
import com.util.FileUtil;
import com.util.KlinesComparator;
import com.util.PriceUtil;
import com.util.StraightLineUtil;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@Service("klinesService")
public class KlinesServiceImpl implements KlinesService {

	private final Logger logger = LogManager.getLogger(KlinesServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private UserService userDetailsService;
	
	@Resource
	private UserRepository userRepository;
	
	@Autowired
	private WorkTaskPool emailWorkTaskPool;
	
	@Autowired
	private KlinesRepository klinesRepository;
	
	@Autowired
	private BinanceRestTradeService binanceRestTradeService;
	
	@Autowired
	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	@Autowired
	private ShapeRepository shapeRepository;
	
	@Autowired
	private EmailRepository emailRepository;
	
	@Override
	public List<Klines> continuousKlines(String pair, long startTime, long endTime,
			Inerval interval,QUERY_SPLIT split) {
		
				UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(AppConfig.REST_BASE_URL + "/fapi/v1/continuousKlines")
				.queryParam("pair", pair)
				.queryParam("contractType", "PERPETUAL")
				.queryParam("startTime", startTime)
				.queryParam("interval", interval.getDescption())
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
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		
		return CommandUtil.format(pair, result, interval.getDescption());
	}

	@Override
	public List<Klines> continuousKlines1Day(String pair, Date now, int limit,QUERY_SPLIT split) {
		
		int hours = DateFormatUtil.getHours(now.getTime());
		Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
		Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
		
		Date firstDayStartTime = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -limit);//多少天以前起始时间
		
		return continuousKlines(pair, firstDayStartTime.getTime(), 
				lastDayEndTimeDate.getTime() + 999, Inerval.INERVAL_1D,split);
	}

	@Override
	public List<Klines> continuousKlines5M(String pair, Date now, int limit,QUERY_SPLIT split) {
		List<Klines> result = null;
		try {
			
			Date endTime_5m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
			Date startTime_5m = DateFormatUtil.getStartTimeBySetMinute(endTime_5m, -Inerval.INERVAL_5M.getNumber() * limit);//limit根k线
			endTime_5m = DateFormatUtil.getStartTimeBySetMillisecond(endTime_5m, -1);//收盘时间
			
			result = continuousKlines(pair, startTime_5m.getTime(),
					endTime_5m.getTime(), Inerval.INERVAL_5M,split);
			
		} catch (Exception e) {
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
					endTime.getTime(), Inerval.INERVAL_15M,split);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public void openLong(FibUtil_v3 fu,FibInfo fibInfo,Klines afterLowKlines,List<Klines> klinesList_hit) {
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePriceDoubleValue();
		//double openPrice = hitKline.getOpenPrice();
		//double lowPrice = hitKline.getLowPriceDoubleValue();
		//double hightPrice = hitKline.getHighPrice();
		double currentPrice = closePrice;
		
		//次级回撤信息
		FibLevel level = fibInfo.getLevel();
		FibInfo childFibInfo = null;
		if(level == FibLevel.LEVEL_1) {
			childFibInfo = fu.getSecondFibInfo(fibInfo);
		} else if(level == FibLevel.LEVEL_2) {
			childFibInfo = fu.getThirdFibInfo(fibInfo);
		} else if(level == FibLevel.LEVEL_3) {
			childFibInfo = fu.getFourthFibInfo(fibInfo);
		}
		
		//多头行情做多 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 FIB0
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];//当前斐波那契点位
			
			if(PriceUtil.isLong_v3(fibInfo.getFibValue(code), klinesList_hit)
					&& fu.verifyParentOpen(fibInfo, code, childFibInfo)
					&& !PriceUtil.isObsoleteLong(fibInfo,afterLowKlines,codes,offset)) {
				
				//市价做多
				marketPlace(pair,PositionSide.LONG, 0, 0, offset, fibInfo, AutoTradeType.FIB_RET);
				
				//
				List<User> userList = userRepository.queryAllUserByFibMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					FibCode closePpositionCode = fibInfo.getTakeProfit_v7(code);//止盈点位
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getRiseFluctuationPercentage(currentPrice, fibInfo.getFibValue(closePpositionCode)) * 100;
					
					if(profitPercent < u.getProfit()) {
						continue;
					}
					
					//止盈价
					double profitPrice = fibInfo.getFibValue(closePpositionCode);

					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						if(profitPercent > u.getProfitLimit()) {
							profitPercent = u.getProfitLimit();
						}
						profitPrice = PriceUtil.getLongTakeProfitForPercent(currentPrice, profitPercent / 100);
					}
					
					//开仓订阅提醒
					String subject = String.format("%s永续合约%s(%s)[%s]做多机会(PNL:%s%%) %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatLongMessage(pair, currentPrice, PriceUtil.rectificationCutLossLongPrice_v3(currentPrice, u.getCutLoss()), 
							profitPrice, fibInfo.getDecimalPoint());
					
					text += "\r\n\r\n" + fibInfo.toString();
					
					sendEmail(subject, text, u.getUsername());
				}
				
			}
			
			if(code == fibInfo.getLevel().getStartFibCode()) {
				break;
			}
		}
	}
	
	@Override
	public void openLong_v2(FibInfo fibInfo,Klines afterLowKlines,List<Klines> klinesList_hit) {
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePriceDoubleValue();
		double currentPrice = closePrice;
		
		//多头行情做多 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 FIB0
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];//当前斐波那契点位
			
			if(PriceUtil.isLong_v3(fibInfo.getFibValue(code), klinesList_hit)
					&& !PriceUtil.isObsoleteLong(fibInfo,afterLowKlines,codes,offset)) {
				
				//市价做多
				marketPlace(pair,PositionSide.LONG, 0, 0, offset, fibInfo, AutoTradeType.FIB_RET);
				
				//
				List<User> userList = userRepository.queryAllUserByFibMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					FibCode closePpositionCode = fibInfo.getTakeProfit_v8(code);//止盈点位
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getRiseFluctuationPercentage(currentPrice, fibInfo.getFibValue(closePpositionCode)) * 100;
					
					if(profitPercent < u.getProfit()) {
						continue;
					}
					
					//止盈价
					double profitPrice = fibInfo.getFibValue(closePpositionCode);

					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						if(profitPercent > u.getProfitLimit()) {
							profitPercent = u.getProfitLimit();
						}
						profitPrice = PriceUtil.getLongTakeProfitForPercent(currentPrice, profitPercent / 100);
					}
					
					//开仓订阅提醒
					String subject = String.format("%s永续合约%s(%s)[%s]做多机会(PNL:%s%%) %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatLongMessage(pair, currentPrice, PriceUtil.rectificationCutLossLongPrice_v3(currentPrice, u.getCutLoss()), 
							profitPrice, fibInfo.getDecimalPoint());
					
					text += "\r\n\r\n" + fibInfo.toString();
					
					sendEmail(subject, text, u.getUsername());
				}
				
			}
			
			if(code == fibInfo.getLevel().getStartFibCode()) {
				break;
			}
		}
	}
	
	@Override
	public void openShort(FibUtil_v3 fu,FibInfo fibInfo,Klines afterHighKlines,List<Klines> klinesList_hit) {
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePriceDoubleValue();
		//double openPrice = hitKline.getOpenPrice();
		//double lowPrice = hitKline.getLowPrice();
		//double hightPrice = hitKline.getHighPriceDoubleValue();
		double currentPrice = closePrice;
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//次级回撤信息
		FibLevel level = fibInfo.getLevel();
		FibInfo childFibInfo = null;
		if(level == FibLevel.LEVEL_1) {
			childFibInfo = fu.getSecondFibInfo(fibInfo);
		} else if(level == FibLevel.LEVEL_2) {
			childFibInfo = fu.getThirdFibInfo(fibInfo);
		} else if(level == FibLevel.LEVEL_3) {
			childFibInfo = fu.getFourthFibInfo(fibInfo);
		}
		
		//空头行情做空 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 FIB0
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];//当前斐波那契点位
			
			if(PriceUtil.isShort_v3(fibInfo.getFibValue(code), klinesList_hit)
					&& fu.verifyParentOpen(fibInfo, code, childFibInfo)
					&& !PriceUtil.isObsoleteShort(fibInfo,afterHighKlines,codes,offset)) {
				
				//市价做空
				marketPlace(pair, PositionSide.SHORT, 0, 0, offset,  fibInfo, AutoTradeType.FIB_RET);

				//
				List<User> userList = userRepository.queryAllUserByFibMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					FibCode closePpositionCode = fibInfo.getTakeProfit_v7(code);//止盈点位
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getFallFluctuationPercentage(currentPrice, fibInfo.getFibValue(closePpositionCode)) * 100;
					
					if(profitPercent < u.getProfit()) {
						continue;
					}
					
					//止盈价
					double profitPrice = fibInfo.getFibValue(closePpositionCode);
					
					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						if(profitPercent > u.getProfitLimit()) {
							profitPercent = u.getProfitLimit();
						}
						profitPrice = PriceUtil.getShortTakeProfitForPercent(currentPrice, profitPercent / 100);
					}
					
					String subject = String.format("%s永续合约%s(%s)[%s]做空机会(PNL:%s%%) %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatShortMessage(pair, currentPrice, profitPrice, 
							PriceUtil.rectificationCutLossShortPrice_v3(currentPrice, u.getCutLoss()), fibInfo.getDecimalPoint());
					
					text += "\r\n\r\n" + fibInfo.toString();
					
					
					sendEmail(subject,text, u.getUsername());
				}
			}

			
			if(code == fibInfo.getLevel().getStartFibCode()) {
				break;
			}
		}
	}
	
	@Override
	public void openShort_v2(FibInfo fibInfo,Klines afterHighKlines,List<Klines> klinesList_hit) {
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePriceDoubleValue();
		double currentPrice = closePrice;
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//空头行情做空 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 FIB0
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];//当前斐波那契点位
			
			if(PriceUtil.isShort_v3(fibInfo.getFibValue(code), klinesList_hit)
					&& !PriceUtil.isObsoleteShort(fibInfo,afterHighKlines,codes,offset)) {
				
				//市价做空
				marketPlace(pair, PositionSide.SHORT, 0, 0, offset,  fibInfo, AutoTradeType.FIB_RET);

				//
				List<User> userList = userRepository.queryAllUserByFibMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					FibCode closePpositionCode = fibInfo.getTakeProfit_v8(code);//止盈点位
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getFallFluctuationPercentage(currentPrice, fibInfo.getFibValue(closePpositionCode)) * 100;
					
					if(profitPercent < u.getProfit()) {
						continue;
					}
					
					//止盈价
					double profitPrice = fibInfo.getFibValue(closePpositionCode);
					
					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						if(profitPercent > u.getProfitLimit()) {
							profitPercent = u.getProfitLimit();
						}
						profitPrice = PriceUtil.getShortTakeProfitForPercent(currentPrice, profitPercent / 100);
					}
					
					String subject = String.format("%s永续合约%s(%s)[%s]做空机会(PNL:%s%%) %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatShortMessage(pair, currentPrice, profitPrice, 
							PriceUtil.rectificationCutLossShortPrice_v3(currentPrice, u.getCutLoss()), fibInfo.getDecimalPoint());
					
					text += "\r\n\r\n" + fibInfo.toString();
					
					
					sendEmail(subject,text, u.getUsername());
				}
			}

			
			if(code == fibInfo.getLevel().getStartFibCode()) {
				break;
			}
		}
	}
	
	@Override
	public void marketPlace(String pair,PositionSide ps, double stopLossDoubleValue, double takeProfitDoubleValue, int offset, FibInfo fibInfo, AutoTradeType autoTradeType) {
		FibCode[] codes = FibCode.values();
		if(ps == PositionSide.LONG) {//做多
			//只做U本位(USDT)合约
			if(pair.endsWith("USDT")) {
				List<User> userList = null;
				if(autoTradeType == AutoTradeType.DEFAULT) {
					userList = userDetailsService.queryByDrawTrade(DrawTrade.OPEN);
				} else {
					userList = userDetailsService.queryByAutoTrade(AutoTrade.OPEN, autoTradeType);
				}
				for(User u : userList) {
					String binanceApiKey = u.getBinanceApiKey();
					String binanceSecretKey = u.getBinanceSecretKey();
					String tradeUserEmail = u.getUsername();
					String dateStr = DateFormatUtil.format(new Date());
					RecvTradeStatus recvTradeStatus = RecvTradeStatus.valueOf(u.getRecvTrade());
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					//计算预计盈利百分比
					double profitPercent = 0;
					
					try {
						
						PriceInfo priceInfo = binanceWebsocketTradeService.getPrice(pair);
						
						if(priceInfo == null) {
							continue;
						}

						int decimalNum = new BigDecimal(String.valueOf(Double.valueOf(priceInfo.getPrice()))).scale();
						
						BigDecimal stopLoss = null;
						BigDecimal takeProfit = null;

						if(fibInfo == null) {//自定义止盈止损
							stopLoss = new BigDecimal(PriceUtil.formatDoubleDecimal(stopLossDoubleValue, decimalNum));
							takeProfit = new BigDecimal(PriceUtil.formatDoubleDecimal(takeProfitDoubleValue , decimalNum));
							
							//计算预计盈利百分比
							profitPercent = PriceUtil.getRiseFluctuationPercentage(Double.valueOf(priceInfo.getPrice()),takeProfit.doubleValue());
							
							//盈利太少则不做交易
							if((profitPercent * 100) < u.getProfit()) {
								logger.debug(pair + "预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%，不做交易");
								continue;
							}
							
						} else {
							
							//止盈点位
							FibCode takeProfitCode = FibCode.FIB618;
							
							if(autoTradeType == AutoTradeType.FIB_RET) {
								FibCode code = codes[offset];
								
								if(fibInfo.getLevel() == FibLevel.LEVEL_5) {
									takeProfitCode = fibInfo.getTakeProfit_v8(code);
								} else {
									takeProfitCode = fibInfo.getTakeProfit_v7(code);
								}
								
								logger.debug("当前交易风格：{},所处点位：{}，止盈点位：{}", tradeStyle.getMemo(), code.getDescription(), takeProfitCode.getDescription());
								
								//回踩单判断
								TradeStepBackStatus tradeStepBackStatus = TradeStepBackStatus.valueOf(u.getTradeStepBack());
								if(code.gt(FibCode.FIB1) && tradeStepBackStatus == TradeStepBackStatus.CLOSE) {
									continue;
								}
							} else if(autoTradeType == AutoTradeType.EMA_INDEX) {
								takeProfitCode = FibCode.FIB618;
							} else if(autoTradeType == AutoTradeType.AREA_INDEX) {
								takeProfitCode = FibCode.FIB618;
							} else if(autoTradeType == AutoTradeType.PRICE_ACTION) {
								takeProfitCode = FibCode.FIB618;
							}
							
							stopLoss = new BigDecimal(
									PriceUtil.formatDoubleDecimal(PriceUtil.rectificationCutLossLongPrice_v3(Double.valueOf(priceInfo.getPrice()), u.getCutLoss()),decimalNum));
							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(takeProfitCode),decimalNum)
											);
							
							//计算预计盈利百分比
							profitPercent = PriceUtil.getRiseFluctuationPercentage(Double.valueOf(priceInfo.getPrice()),takeProfit.doubleValue());
							
							//盈利太少则不做交易
							if((profitPercent * 100) < u.getProfit()) {
								logger.debug(pair + "预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%，不做交易");
								continue;
							}
							
							//根据交易风格设置盈利限制
							if(tradeStyle == TradeStyle.CONSERVATIVE) {
								double dbProfitLimit = u.getProfitLimit() / 100;
								if(profitPercent > dbProfitLimit) {
									profitPercent = dbProfitLimit;
								}
								takeProfit = new BigDecimal(
										PriceUtil.formatDoubleDecimal( PriceUtil.getLongTakeProfitForPercent(priceInfo.getPriceDoubleValue(), profitPercent) ,decimalNum)
												);
								logger.debug("交易对：{}，当前价格：{}，波动幅度：{}，止盈价格：{}",pair,priceInfo.getPriceDoubleValue(),profitPercent,
										PriceUtil.formatDoubleDecimal( PriceUtil.getLongTakeProfitForPercent(priceInfo.getPriceDoubleValue(), profitPercent) ,decimalNum));
							}
							
						}
						
						//多头的止盈价格必须大于当前价格
						if(!(priceInfo.getPriceDoubleValue() < takeProfit.doubleValue() && priceInfo.getPriceDoubleValue() > stopLoss.doubleValue())) {
							continue;
						}
						
						List<BinanceOrderInfo> orderList = binanceRestTradeService.openOrders(binanceApiKey, binanceSecretKey, pair);
						if(!CollectionUtils.isEmpty(orderList)) {
							logger.debug("用户" + u.getUsername() + "在" + pair + "交易对中已有持仓");
							continue;
						}
						
						//最少下单数量
						String quantityNum = binanceWebsocketTradeService.getMarketMinQuantity(pair);
						BigDecimal minQuantity = new BigDecimal(quantityNum);
						
						//预期持仓数量
						BigDecimal quantity = minQuantity.multiply(new BigDecimal(u.getBaseStepSize()));

						logger.debug("{}交易对多头预期持仓数量：{}", pair, quantity);
						
						//修正持仓数量
						quantity = PriceUtil.rectificationQuantity(quantity, minQuantity, u.getBaseStepSize(), u.getPositionValue(), priceInfo);
						
						logger.debug("{}交易对多头修正后持仓数量：{}", pair, quantity);
						
						if(quantity.doubleValue() == 0) {
							continue;
						}
						
						//查询杠杆
						SymbolConfig sc = binanceRestTradeService.getSymbolConfigBySymbol(binanceApiKey, binanceSecretKey, pair);

						int leverage = sc.getLeverage();
						
						//持仓价值 = 持仓数量 * 价格
						double order_value = quantity.doubleValue() * priceInfo.getPriceDoubleValue();
						
						if(order_value > u.getPositionValue()) {
							logger.debug(pair + "下单数量仓位价值超过" + u.getPositionValue() + "USDT");
							continue;
						}
						
						double minOrder_value = (order_value / leverage) * 1.5;
						
						String availableBalanceStr = binanceWebsocketTradeService.availableBalance(binanceApiKey, binanceSecretKey, "USDT");
						if(Double.valueOf(availableBalanceStr) < minOrder_value) {
							logger.debug("用户" + u.getUsername() + "可下单金额小于" + minOrder_value + "USDT");
							continue;
						}
						
						boolean dualSidePosition = binanceRestTradeService.dualSidePosition(binanceApiKey, binanceSecretKey);
						logger.debug("当前持仓模式：" + (dualSidePosition ? "双向持仓" : "单向持仓"));
						if(!dualSidePosition) {
							logger.debug("开始修改持仓模式为双向持仓");
							Result result = binanceRestTradeService.dualSidePosition(binanceApiKey, binanceSecretKey, true);
							if(result.getResult() == ResultCode.SUCCESS) {
								logger.debug("修改持仓模式成功");
							} else {
								logger.debug("修改持仓模式失败，失败原因：" + result.getMsg());
								sendEmail("修改持仓模式失败 " + dateStr, "修改持仓模式失败，失败原因：" + result.getMsg(), tradeUserEmail);
							}
						}
						
						MarginType marginType = MarginType.resolve(sc.getMarginType());
						
						logger.debug(pair + "当前保证金模式：" + marginType);
						
						if(marginType != MarginType.ISOLATED) {
							logger.debug("修改" + pair + "保证金模式为：" + MarginType.ISOLATED);
							binanceRestTradeService.marginType(binanceApiKey, binanceSecretKey, pair, MarginType.ISOLATED);
						}
						
						logger.debug(pair + "当前杠杆倍数：" + leverage + "倍");
						if(leverage != u.getLeverage()) {
							logger.debug("开始修改" + pair + "杠杆倍数");
							binanceRestTradeService.leverage(binanceApiKey, binanceSecretKey, pair, u.getLeverage());
						}
						
						binanceWebsocketTradeService.tradeMarket(binanceApiKey, binanceSecretKey, pair, PositionSide.LONG, quantity, stopLoss, takeProfit);

						//开仓邮件通知
						String subject_ = "";
						String pnlStr = PriceUtil.formatDoubleDecimal(profitPercent * 100, 2);
						
						if(autoTradeType == AutoTradeType.FIB_RET && fibInfo != null) {
							subject_ = String.format("%s多头仓位已买入[%s][%s(%s)][PNL:%s%%] %s", 
									pair, 
									fibInfo.getLevel().getLabel(),
									codes[offset].getDescription(), 
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(codes[offset]),fibInfo.getDecimalPoint()),
									pnlStr, 
									dateStr);
						} else {
							subject_ = String.format("%s多头仓位已买入[PNL:%s%%] %s", pair, pnlStr, dateStr);
						}
						
						String text_ = StringUtil.formatLongMessage_v2(pair, Double.valueOf(priceInfo.getPrice()), stopLoss.doubleValue(), 
								takeProfit.doubleValue(), decimalNum, pnlStr);
						
						if(fibInfo != null) {
							text_ += "\n\n" + fibInfo.toString();
						}
						
						if(recvTradeStatus == RecvTradeStatus.OPEN) {
							sendEmail(subject_, text_, tradeUserEmail);
						}
						
					} catch (Exception e) {
						sendEmail("创建" + pair + "多头仓位时出现异常 " + dateStr, e.getMessage(), tradeUserEmail);
						logger.error(e.getMessage(), e);
					}
					
				}
			}
		} else {
			//只做U本位(USDT)合约
			if(pair.endsWith("USDT")) {
				List<User> userList = null;
				if(autoTradeType == AutoTradeType.DEFAULT) {
					userList = userDetailsService.queryByDrawTrade(DrawTrade.OPEN);
				} else {
					userList = userDetailsService.queryByAutoTrade(AutoTrade.OPEN, autoTradeType);
				}
				for(User u : userList) {
					
					String binanceApiKey = u.getBinanceApiKey();
					String binanceSecretKey = u.getBinanceSecretKey();
					String tradeUserEmail = u.getUsername();
					String dateStr = DateFormatUtil.format(new Date());
					RecvTradeStatus recvTradeStatus = RecvTradeStatus.valueOf(u.getRecvTrade());
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					//计算预计盈利百分比
					double profitPercent = 0;
					
					try {

						PriceInfo priceInfo = binanceWebsocketTradeService.getPrice(pair);
						
						if(priceInfo == null) {
							continue;
						}
						
						int decimalNum = new BigDecimal(String.valueOf(Double.valueOf(priceInfo.getPrice()))).scale();
						
						BigDecimal stopLoss = null;
						BigDecimal takeProfit = null; 

						if(fibInfo == null) {//自定义止盈止损
							stopLoss = new BigDecimal(PriceUtil.formatDoubleDecimal(stopLossDoubleValue, decimalNum));
							takeProfit = new BigDecimal(PriceUtil.formatDoubleDecimal(takeProfitDoubleValue , decimalNum));
							
							//计算预计盈利百分比
							profitPercent = PriceUtil.getFallFluctuationPercentage(Double.valueOf(priceInfo.getPrice()),takeProfit.doubleValue());
							
							//盈利太少则不做交易
							if((profitPercent * 100) < u.getProfit()) {
								logger.debug(pair + "预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%，不做交易");
								continue;
							}
						} else {
							//止盈点位
							FibCode takeProfitCode = FibCode.FIB618;
							
							if(autoTradeType == AutoTradeType.FIB_RET) {
								FibCode code = codes[offset];

								if(fibInfo.getLevel() == FibLevel.LEVEL_5) {
									takeProfitCode = fibInfo.getTakeProfit_v8(code);
								} else {
									takeProfitCode = fibInfo.getTakeProfit_v7(code);
								}
								
								logger.debug("当前交易风格：{},所处点位：{}，止盈点位：{}", tradeStyle.getMemo(), code.getDescription(), takeProfitCode.getDescription());
								
								//回踩单判断
								TradeStepBackStatus tradeStepBackStatus = TradeStepBackStatus.valueOf(u.getTradeStepBack());
								if(code.gt(FibCode.FIB1) && tradeStepBackStatus == TradeStepBackStatus.CLOSE) {
									continue;
								}
							} else if(autoTradeType == AutoTradeType.EMA_INDEX) {
								takeProfitCode = FibCode.FIB618;
							} else if(autoTradeType == AutoTradeType.AREA_INDEX) {
								takeProfitCode = FibCode.FIB618;
							} else if(autoTradeType == AutoTradeType.PRICE_ACTION) {
								takeProfitCode = FibCode.FIB618;
							}
							
							stopLoss = new BigDecimal(
									PriceUtil.formatDoubleDecimal(PriceUtil.rectificationCutLossShortPrice_v3(Double.valueOf(priceInfo.getPrice()), u.getCutLoss()), decimalNum)
											);
							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(takeProfitCode),decimalNum)
											);
							
							//计算预计盈利百分比
							profitPercent = PriceUtil.getFallFluctuationPercentage(Double.valueOf(priceInfo.getPrice()),takeProfit.doubleValue());
							
							//盈利太少则不做交易
							if((profitPercent * 100) < u.getProfit()) {
								logger.debug(pair + "预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%，不做交易");
								continue;
							}
							
							//根据交易风格设置盈利限制
							if(tradeStyle == TradeStyle.CONSERVATIVE) {
								double dbProfitLimit = u.getProfitLimit() / 100;
								if(profitPercent > dbProfitLimit) {
									profitPercent = dbProfitLimit;
								}
								
								takeProfit = new BigDecimal(
										PriceUtil.formatDoubleDecimal( PriceUtil.getShortTakeProfitForPercent(priceInfo.getPriceDoubleValue(), profitPercent) ,decimalNum)
												);
								logger.debug("交易对：{}，当前价格：{}，波动幅度：{}，止盈价格：{}",pair,priceInfo.getPriceDoubleValue(),profitPercent,
										PriceUtil.formatDoubleDecimal( PriceUtil.getShortTakeProfitForPercent(priceInfo.getPriceDoubleValue(), profitPercent) ,decimalNum));
							}
						}
						
						//空头止盈价格必须小于当前价格
						if(!(priceInfo.getPriceDoubleValue() > takeProfit.doubleValue() && priceInfo.getPriceDoubleValue() < stopLoss.doubleValue())) {
							continue;
						}

						List<BinanceOrderInfo> orderList = binanceRestTradeService.openOrders(binanceApiKey, binanceSecretKey, pair);
						if(!CollectionUtils.isEmpty(orderList)) {
							logger.debug("用户" + u.getUsername() + "在" + pair + "交易对中已有持仓");
							continue;
						}

						//最少下单数量
						String quantityNum = binanceWebsocketTradeService.getMarketMinQuantity(pair);
						BigDecimal minQuantity = new BigDecimal(quantityNum);
						
						//预期持仓数量
						BigDecimal quantity = minQuantity.multiply(new BigDecimal(u.getBaseStepSize()));

						logger.debug("{}交易对空头预期持仓数量：{}", pair, quantity);
						
						//修正持仓数量
						quantity = PriceUtil.rectificationQuantity(quantity, minQuantity, u.getBaseStepSize(), u.getPositionValue(), priceInfo);
						
						logger.debug("{}交易对空头修正后持仓数量：{}", pair, quantity);
						
						if(quantity.doubleValue() == 0) {
							continue;
						}
						
						//查询杠杆
						SymbolConfig sc = binanceRestTradeService.getSymbolConfigBySymbol(binanceApiKey, binanceSecretKey, pair);

						int leverage = sc.getLeverage();
						
						//持仓价值 = 持仓数量 * 价格
						double order_value = quantity.doubleValue() * priceInfo.getPriceDoubleValue();
						
						if(order_value > u.getPositionValue()) {
							logger.debug(pair + "下单数量仓位价值超过" + u.getPositionValue() + "USDT");
							continue;
						}
						
						double minOrder_value = (order_value / leverage) * 1.5;
						
						String availableBalanceStr = binanceWebsocketTradeService.availableBalance(binanceApiKey, binanceSecretKey, "USDT");
						if(Double.valueOf(availableBalanceStr) < minOrder_value) {
							logger.debug("用户" + u.getUsername() + "可下单金额小于" + minOrder_value + "USDT");
							continue;
						}
						
						boolean dualSidePosition = binanceRestTradeService.dualSidePosition(binanceApiKey, binanceSecretKey);
						logger.debug("当前持仓模式：" + (dualSidePosition ? "双向持仓" : "单向持仓"));
						if(!dualSidePosition) {
							logger.debug("开始修改持仓模式为双向持仓");
							Result result = binanceRestTradeService.dualSidePosition(binanceApiKey, binanceSecretKey, true);
							if(result.getResult() == ResultCode.SUCCESS) {
								logger.debug("修改持仓模式成功");
							} else {
								logger.debug("修改持仓模式失败，失败原因：" + result.getMsg());
								sendEmail("修改持仓模式失败 " + dateStr, "修改持仓模式失败，失败原因：" + result.getMsg(), tradeUserEmail);
							}
						}
						
						MarginType marginType = MarginType.resolve(sc.getMarginType());

						logger.debug(pair + "当前保证金模式：" + marginType);
						
						if(marginType != MarginType.ISOLATED) {
							logger.debug("修改" + pair + "保证金模式为：" + MarginType.ISOLATED);
							binanceRestTradeService.marginType(binanceApiKey, binanceSecretKey, pair, MarginType.ISOLATED);
						}
						
						logger.debug(pair + "当前杠杆倍数：" + leverage + "倍");
						
						if(leverage != u.getLeverage()) {
							logger.debug("开始修改" + pair + "杠杆倍数");
							binanceRestTradeService.leverage(binanceApiKey, binanceSecretKey, pair, u.getLeverage());
						}
						
						
						binanceWebsocketTradeService.tradeMarket(binanceApiKey, binanceSecretKey, pair, PositionSide.SHORT, quantity, stopLoss, takeProfit);
						
						//开仓邮件通知
						String subject_ = "";
						String pnlStr = PriceUtil.formatDoubleDecimal(profitPercent * 100, 2);
						
						if(autoTradeType == AutoTradeType.FIB_RET && fibInfo != null) {
							subject_ = String.format("%s空头仓位已卖出[%s][%s(%s)][PNL:%s%%] %s", 
									pair, 
									fibInfo.getLevel().getLabel(),
									codes[offset].getDescription(), 
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(codes[offset]),fibInfo.getDecimalPoint()),
									pnlStr, 
									dateStr);
						} else {
							subject_ = String.format("%s空头仓位已卖出[PNL:%s%%] %s", pair, pnlStr, dateStr);
						}
						
						String text_ = StringUtil.formatShortMessage_v2(pair, Double.valueOf(priceInfo.getPrice()), takeProfit.doubleValue(), 
								stopLoss.doubleValue(), decimalNum, pnlStr);
						
						if(fibInfo != null) {
							text_ += "\n\n" + fibInfo.toString();
						}
						
						if(recvTradeStatus == RecvTradeStatus.OPEN) {
							sendEmail(subject_, text_, tradeUserEmail);
						}
						
					} catch (Exception e) {
						sendEmail("创建" + pair + "空头仓位时出现异常 " + dateStr, e.getMessage(), tradeUserEmail);
						logger.error(e.getMessage(), e);
					}
					
				}
			}
		}
	}
	
	public void sendEmail(String subject,String text,String recEmail) {
		
	 	if(StringUtil.isNotEmpty(recEmail) && StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
			emailWorkTaskPool.add(new SendMailTask(subject, text, recEmail, emailRepository));
	 	}
		
	}

	@Override
	public void futuresHighOrLowMonitor(List<Klines> klinesList,List<Klines> klinesList_hit) {
		
		Date now = new Date();
		String dateStr = DateFormatUtil.format(now);
		
		Klines current = PriceUtil.getLastKlines(klinesList_hit);
		Klines last = PriceUtil.getLastKlines(klinesList);
		String pair = last.getPair();
		
		FibUtil_v3 fu = new FibUtil_v3(klinesList);
		FibInfo firstFibInfo = fu.getFibInfo();
		FibInfo secondFibInfo = fu.getSecondFibInfo(firstFibInfo);
		FibInfo thirdFibInfo = fu.getThirdFibInfo(secondFibInfo);
		FibInfo fourthFibInfo = fu.getFourthFibInfo(thirdFibInfo);
		
		QuotationMode qm = null;
		double highPrice = 0;
		double lowPrice = 0;
		
		Klines low = null;
		Klines high = null;
		
		if(firstFibInfo != null) {
			qm = firstFibInfo.getQuotationMode();
			if(qm == QuotationMode.LONG) {
				low = fu.getFirstStart();
				high = fu.getFirstEnd();
			} else {
				high = fu.getFirstStart();
				low = fu.getFirstEnd();
			}
			highPrice = high.getHighPriceDoubleValue();
			lowPrice = low.getLowPriceDoubleValue();
			//高点做空
			if(PriceUtil.isBreachShort(current, highPrice)) {
				String subject = String.format("%s永续合约突破(%s)并收回 %s", pair,PriceUtil.formatDoubleDecimal(highPrice, high.getDecimalNum()),dateStr);
				
				String text = String.format("%s永续合约突破(%s)最高价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(high.getStartTime())), 
						PriceUtil.formatDoubleDecimal(highPrice, high.getDecimalNum()));
				
				String recEmail = userDetailsService.getHighOrLowMonitorUserEmail();
			 	sendEmail(subject, text, recEmail);
			}
			//低点做多
			if(PriceUtil.isBreachLong(current, lowPrice)) {
				String subject = String.format("%s永续合约跌破(%s)并收回 %s", pair,PriceUtil.formatDoubleDecimal(lowPrice, low.getDecimalNum()),dateStr);
				
				String text = String.format("%s永续合约跌破(%s)最低价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(low.getStartTime())), 
						PriceUtil.formatDoubleDecimal(lowPrice, low.getDecimalNum()));
				
				String recEmail = userDetailsService.getHighOrLowMonitorUserEmail();
			 	sendEmail(subject, text, recEmail);
			}
		}
		
		if(secondFibInfo != null) {
			qm = secondFibInfo.getQuotationMode();
			if(qm == QuotationMode.LONG) {
				low = fu.getSecondStart();
				high = fu.getSecondEnd();
			} else {
				high = fu.getSecondStart();
				low = fu.getSecondEnd();
			}
			highPrice = high.getHighPriceDoubleValue();
			lowPrice = low.getLowPriceDoubleValue();
			//高点做空
			if(PriceUtil.isBreachShort(current, highPrice)) {
				String subject = String.format("%s永续合约突破(%s)并收回 %s", pair,PriceUtil.formatDoubleDecimal(highPrice, high.getDecimalNum()),dateStr);
				
				String text = String.format("%s永续合约突破(%s)最高价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(high.getStartTime())), 
						PriceUtil.formatDoubleDecimal(highPrice, high.getDecimalNum()));
				
				String recEmail = userDetailsService.getHighOrLowMonitorUserEmail();
			 	sendEmail(subject, text, recEmail);
			}
			//低点做多
			if(PriceUtil.isBreachLong(current, lowPrice)) {
				String subject = String.format("%s永续合约跌破(%s)并收回 %s", pair,PriceUtil.formatDoubleDecimal(lowPrice, low.getDecimalNum()),dateStr);
				
				String text = String.format("%s永续合约跌破(%s)最低价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(low.getStartTime())), 
						PriceUtil.formatDoubleDecimal(lowPrice, low.getDecimalNum()));
				
				String recEmail = userDetailsService.getHighOrLowMonitorUserEmail();
			 	sendEmail(subject, text, recEmail);
			}
		}
		
		if(thirdFibInfo != null) {
			qm = thirdFibInfo.getQuotationMode();
			if(qm == QuotationMode.LONG) {
				low = fu.getThirdStart();
				high = fu.getThirdEnd();
			} else {
				high = fu.getThirdStart();
				low = fu.getThirdEnd();
			}
			highPrice = high.getHighPriceDoubleValue();
			lowPrice = low.getLowPriceDoubleValue();
			//高点做空
			if(PriceUtil.isBreachShort(current, highPrice)) {
				String subject = String.format("%s永续合约突破(%s)并收回 %s", pair,PriceUtil.formatDoubleDecimal(highPrice, high.getDecimalNum()),dateStr);
				
				String text = String.format("%s永续合约突破(%s)最高价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(high.getStartTime())), 
						PriceUtil.formatDoubleDecimal(highPrice, high.getDecimalNum()));
				
				String recEmail = userDetailsService.getHighOrLowMonitorUserEmail();
			 	sendEmail(subject, text, recEmail);
			}
			//低点做多
			if(PriceUtil.isBreachLong(current, lowPrice)) {
				String subject = String.format("%s永续合约跌破(%s)并收回 %s", pair,PriceUtil.formatDoubleDecimal(lowPrice, low.getDecimalNum()),dateStr);
				
				String text = String.format("%s永续合约跌破(%s)最低价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(low.getStartTime())), 
						PriceUtil.formatDoubleDecimal(lowPrice, low.getDecimalNum()));
				
				String recEmail = userDetailsService.getHighOrLowMonitorUserEmail();
			 	sendEmail(subject, text, recEmail);
			}
		}
		
		if(fourthFibInfo != null) {
			qm = fourthFibInfo.getQuotationMode();
			if(qm == QuotationMode.LONG) {
				low = fu.getFourthStart();
				high = fu.getFourthEnd();
			} else {
				high = fu.getFourthStart();
				low = fu.getFourthEnd();
			}
			highPrice = high.getHighPriceDoubleValue();
			lowPrice = low.getLowPriceDoubleValue();
			//高点做空
			if(PriceUtil.isBreachShort(current, highPrice)) {
				String subject = String.format("%s永续合约突破(%s)并收回 %s", pair,PriceUtil.formatDoubleDecimal(highPrice, high.getDecimalNum()),dateStr);
				
				String text = String.format("%s永续合约突破(%s)最高价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(high.getStartTime())), 
						PriceUtil.formatDoubleDecimal(highPrice, high.getDecimalNum()));
				
				String recEmail = userDetailsService.getHighOrLowMonitorUserEmail();
			 	sendEmail(subject, text, recEmail);
			}
			//低点做多
			if(PriceUtil.isBreachLong(current, lowPrice)) {
				String subject = String.format("%s永续合约跌破(%s)并收回 %s", pair,PriceUtil.formatDoubleDecimal(lowPrice, low.getDecimalNum()),dateStr);
				
				String text = String.format("%s永续合约跌破(%s)最低价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(low.getStartTime())), 
						PriceUtil.formatDoubleDecimal(lowPrice, low.getDecimalNum()));
				
				String recEmail = userDetailsService.getHighOrLowMonitorUserEmail();
			 	sendEmail(subject, text, recEmail);
			}
		}
	}
	
	@Override
	public void futuresFibMonitor(List<Klines> klinesList,List<Klines> klinesList_hit) {

		Klines last = PriceUtil.getLastKlines(klinesList);
		String pair = last.getPair();
		
		FibUtil_v3 fu = new FibUtil_v3(klinesList);
		FibInfo firstFibInfo = fu.getFibInfo();
		FibInfo secondFibInfo = fu.getSecondFibInfo(firstFibInfo);
		FibInfo thirdFibInfo = fu.getThirdFibInfo(secondFibInfo);
		FibInfo fourthFibInfo = fu.getFourthFibInfo(thirdFibInfo);
		
		//获取最后一天之后的所有参考价格k线信息
		List<Klines> today_hit = PriceUtil.getLastDayAfterKline(last, klinesList_hit);
		//当日最高价k线
		Klines today_high_klines = PriceUtil.getMaxPriceKLine(today_hit);
		//当日最低价k线
		Klines today_low_klines = PriceUtil.getMinPriceKLine(today_hit);
		
		QuotationMode qm = null;
		
		if(firstFibInfo == null) {
			logger.debug("无法计算出{}一级斐波那契回撤信息", pair);
		} else {
			logger.debug("{}一级斐波那契回撤：{}", pair, firstFibInfo.toString());
			qm = firstFibInfo.getQuotationMode();
			//
			List<Klines> fibAfterKlines = fu.getFibAfterKlines();
			if(qm == QuotationMode.LONG) {
				Klines afterLowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);
				
				afterLowKlines = PriceUtil.getMinPriceKlines(today_low_klines, afterLowKlines);
				
				openLong(fu, firstFibInfo, afterLowKlines, klinesList_hit);
			} else if(qm == QuotationMode.SHORT) {
				Klines afterHighKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);
				
				afterHighKlines = PriceUtil.getMaxPriceKlines(today_high_klines, afterHighKlines);
				
				openShort(fu, firstFibInfo, afterHighKlines,klinesList_hit);
			}
		}
		
		if(secondFibInfo == null) {
			logger.debug("无法计算出{}二级斐波那契回撤信息", pair);
		} else {
			logger.debug("{}二级斐波那契回撤：{}", pair, secondFibInfo.toString());
			qm = secondFibInfo.getQuotationMode();
			//
			List<Klines> fibAfterKlines = fu.getSecondFibAfterKlines();
			if(qm == QuotationMode.LONG) {
				Klines afterLowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);

				afterLowKlines = PriceUtil.getMinPriceKlines(today_low_klines, afterLowKlines);
				
				openLong(fu, secondFibInfo, afterLowKlines, klinesList_hit);
			} else if(qm == QuotationMode.SHORT) {
				Klines afterHighKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);

				afterHighKlines = PriceUtil.getMaxPriceKlines(today_high_klines, afterHighKlines);
				
				openShort(fu, secondFibInfo, afterHighKlines,klinesList_hit);
			}
		}
		
		if(thirdFibInfo == null) {
			logger.debug("无法计算出{}三级斐波那契回撤信息", pair);
		} else {
			logger.debug("{}三级斐波那契回撤：{}", pair, thirdFibInfo.toString());
			qm = thirdFibInfo.getQuotationMode();
			List<Klines> fibAfterKlines = fu.getThirdFibAfterKlines();
			if(qm == QuotationMode.LONG) {
				Klines afterLowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);

				afterLowKlines = PriceUtil.getMinPriceKlines(today_low_klines, afterLowKlines);
				
				openLong(fu, thirdFibInfo, afterLowKlines, klinesList_hit);
			} else if(qm == QuotationMode.SHORT) {
				Klines afterHighKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);

				afterHighKlines = PriceUtil.getMaxPriceKlines(today_high_klines, afterHighKlines);
				
				openShort(fu, thirdFibInfo, afterHighKlines,klinesList_hit);
			}
		}
		
		if(fourthFibInfo == null) {
			logger.debug("无法计算出{}四级斐波那契回撤信息", pair);
		} else {
			logger.debug("{}四级斐波那契回撤：{}", pair, fourthFibInfo.toString());
			qm = fourthFibInfo.getQuotationMode();
			List<Klines> fibAfterKlines = fu.getFourthFibAfterKlines();
			if(qm == QuotationMode.LONG) {
				Klines afterLowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);

				afterLowKlines = PriceUtil.getMinPriceKlines(today_low_klines, afterLowKlines);
				
				openLong(fu, fourthFibInfo, afterLowKlines, klinesList_hit);
			} else if(qm == QuotationMode.SHORT) {
				Klines afterHighKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);

				afterHighKlines = PriceUtil.getMaxPriceKlines(today_high_klines, afterHighKlines);
				
				openShort(fu, fourthFibInfo, afterHighKlines,klinesList_hit);
			}
		}
	}
	
	@Override
	public void futuresFibMonitor_v2(List<Klines> klinesList,List<Klines> klinesList_hit) {
		
		FibUtil fu = new FibUtil(klinesList);
		
		FibInfo fibInfo = fu.getFibInfo();
		
		Klines last = PriceUtil.getLastKlines(klinesList);
		String pair = last.getPair();
		
		if(fibInfo == null) {
			logger.info("无法计算出" + pair + "斐波那契回撤信息");
			return;
		}
		
		List<Klines> fibAfterKlines = fu.getFibAfterKlines();
		
		QuotationMode qm = fibInfo.getQuotationMode();
		
		if(qm == QuotationMode.LONG) {
			Klines afterLowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);
			openLong_v2(fibInfo, afterLowKlines, klinesList_hit);
		} else if(qm == QuotationMode.SHORT) {
			Klines afterHighKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);
			openShort_v2(fibInfo, afterHighKlines, klinesList_hit);
		}
	}
	
	@Override
	public void declineAndStrengthCheck(List<Klines> klinesListData) {
		
		if(CollectionUtils.isEmpty(klinesListData)) {
			return;
		}
		
		String text = "";//邮件内容
		String subject = "";//邮件主题
		String dateStr = DateFormatUtil.format(new Date());
		
		List<Klines> klinesList_tmp = new ArrayList<Klines>();
		klinesList_tmp.addAll(klinesListData);
		
		List<Klines> klinesList = PriceUtil.to1HFor15MKlines(klinesList_tmp);
		
		Klines lastKlines = PriceUtil.getLastKlines(klinesListData);
		
		String pair = lastKlines.getPair();
		double closePrice = lastKlines.getClosePriceDoubleValue();
		
		int minute = DateFormatUtil.getMinute(lastKlines.getEndTime());
		if(minute != 59) {
			klinesList.remove(lastKlines);
		}
		
		FibUtil_v2 fu = new FibUtil_v2(klinesList);
		
		FibInfo firstFibInfo = fu.getFibInfo();
		FibInfo secondFibInfo = fu.getSecondFibInfo(firstFibInfo);
		
		double percent = 0;
		FibCode takeProfitCode = FibCode.FIB618;
		
		//二级回撤
		if(PriceUtil.verifyDecliningPrice_v4(secondFibInfo, klinesListData) && fu.verifyFirstFibOpen(firstFibInfo, closePrice)) {
			
			percent = PriceUtil.getFallFluctuationPercentage(closePrice, secondFibInfo.getFibValue(takeProfitCode)) * 100;
			String percentStr = PriceUtil.formatDoubleDecimal(percent, 2);
			subject = String.format("%s永续合约颓势价格行为(PNL:%s%%) %s", pair, percentStr, dateStr);
			
			//市价做空
			marketPlace(pair, PositionSide.SHORT, 0, 0, 0, secondFibInfo, AutoTradeType.PRICE_ACTION);
			
			List<User> uList = userRepository.queryAllUserByEmaMonitor(MonitorStatus.OPEN);
			
			for(User u : uList) {
				
				double profit = u.getProfit();
				double cutLoss = u.getCutLoss();
				
				if(percent < profit) {
					continue;
				}
				
				text = StringUtil.formatShortMessage(pair, closePrice, secondFibInfo, PriceUtil.rectificationCutLossShortPrice_v3(closePrice, cutLoss), takeProfitCode);
				
				text += "，预计盈利：" + percentStr + "%";
				
				if(secondFibInfo != null) {
					text += "\n\n" + secondFibInfo.toString();
				}
				
				sendEmail(subject, text, u.getUsername());
			}
			
		} else if(PriceUtil.verifyPowerful_v4(secondFibInfo, klinesListData) && fu.verifyFirstFibOpen(firstFibInfo, closePrice)) {
			
			percent = PriceUtil.getRiseFluctuationPercentage(closePrice, secondFibInfo.getFibValue(takeProfitCode)) * 100;
			String percentStr = PriceUtil.formatDoubleDecimal(percent, 2);
			subject = String.format("%s永续合约强势价格行为(PNL:%s%%) %s", pair, percentStr, dateStr);
			
			//市价做多
			marketPlace(pair, PositionSide.LONG, 0, 0, 0, secondFibInfo, AutoTradeType.PRICE_ACTION);
			
			List<User> uList = userRepository.queryAllUserByEmaMonitor(MonitorStatus.OPEN);
			
			for(User u : uList) {
				double profit = u.getProfit();
				double cutLoss = u.getCutLoss();
				
				if(percent < profit) {
					continue;
				}
				
				text = StringUtil.formatLongMessage(pair, closePrice, secondFibInfo, PriceUtil.rectificationCutLossLongPrice_v3(closePrice, cutLoss), takeProfitCode);
				
				text += "，预计盈利：" + percentStr + "%";
				
				if(secondFibInfo != null) {
					text += "\n\n" + secondFibInfo.toString();
				}
				
				sendEmail(subject, text, u.getUsername());
			}
			
		}
		
		//一级回撤
		else if(PriceUtil.verifyDecliningPrice_v4(firstFibInfo, klinesListData)) {
			
			percent = PriceUtil.getFallFluctuationPercentage(closePrice, firstFibInfo.getFibValue(takeProfitCode)) * 100;
			String percentStr = PriceUtil.formatDoubleDecimal(percent, 2);
			subject = String.format("%s永续合约颓势价格行为(PNL:%s%%) %s", pair, percentStr, dateStr);

			//市价做空
			marketPlace(pair, PositionSide.SHORT, 0, 0, 0, firstFibInfo, AutoTradeType.PRICE_ACTION);
			
			List<User> uList = userRepository.queryAllUserByEmaMonitor(MonitorStatus.OPEN);
			
			for(User u : uList) {
				
				double profit = u.getProfit();
				double cutLoss = u.getCutLoss();
				
				if(percent < profit) {
					continue;
				}
				
				text = StringUtil.formatShortMessage(pair, closePrice, firstFibInfo, PriceUtil.rectificationCutLossShortPrice_v3(closePrice, cutLoss), takeProfitCode);
				
				text += "，预计盈利：" + percentStr + "%";
				
				if(firstFibInfo != null) {
					text += "\n\n" + firstFibInfo.toString();
				}
				
				sendEmail(subject, text, u.getUsername());
			}
			
		} else if(PriceUtil.verifyPowerful_v4(firstFibInfo, klinesListData)) {
			
			percent = PriceUtil.getRiseFluctuationPercentage(closePrice, firstFibInfo.getFibValue(takeProfitCode)) * 100;
			String percentStr = PriceUtil.formatDoubleDecimal(percent, 2);
			subject = String.format("%s永续合约强势价格行为(PNL:%s%%) %s", pair, percentStr, dateStr);
			
			//市价做多
			marketPlace(pair, PositionSide.LONG, 0, 0, 0, firstFibInfo, AutoTradeType.PRICE_ACTION);
			
			List<User> uList = userRepository.queryAllUserByEmaMonitor(MonitorStatus.OPEN);
			
			for(User u : uList) {
				double profit = u.getProfit();
				double cutLoss = u.getCutLoss();
				
				if(percent < profit) {
					continue;
				}
				
				text = StringUtil.formatLongMessage(pair, closePrice, firstFibInfo, PriceUtil.rectificationCutLossLongPrice_v3(closePrice, cutLoss), takeProfitCode);
				
				text += "，预计盈利：" + percentStr + "%";
				
				if(firstFibInfo != null) {
					text += "\n\n" + firstFibInfo.toString();
				}
				
				sendEmail(subject, text, u.getUsername());
			}
			
		}
		
	}

	@Override
	public void futuresRiseAndFall(List<Klines> klinesList) {

		if(!CollectionUtils.isEmpty(klinesList)){
			int lastIndex = klinesList.size() - 1;

			Klines currentKlines = klinesList.get(lastIndex);

			String pair = currentKlines.getPair();
					
			String percentageStr = PriceUtil.formatDoubleDecimal(PriceUtil.getPriceFluctuationPercentage(klinesList), 2);
			
			double pricePercentage = Double.valueOf(percentageStr);
			
			String text = "";//邮件内容
			String subject = "";//邮件主题
			String dateStr = DateFormatUtil.format(new Date());
			if(PriceUtil.isFall(klinesList)) {//下跌
				
				if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT") || pair.equals("BNBUSDT")) {
					if(pricePercentage >= 5) {
						subject = pair + "永续合约价格大暴跌";
					} else if(pricePercentage >= 3) {
						subject = pair + "永续合约价格暴跌";
					}else if(pricePercentage >= 1.5) {
						subject = pair + "永续合约价格大跌";
					}
				} else {
					if(pricePercentage >= 15) {
						subject = pair + "永续合约价格大暴跌";
					} else if(pricePercentage >= 10) {
						subject = pair + "永续合约价格暴跌";
					}else if(pricePercentage >= 5) {
						subject = pair + "永续合约价格大跌";
					}
				}
				
			} else if(PriceUtil.isRise(klinesList)) {
				if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT") || pair.equals("BNBUSDT")) {
					if(pricePercentage >= 5) {
						subject = pair + "永续合约价格大暴涨";
					} else if(pricePercentage >= 3) {
						subject = pair + "永续合约价格暴涨";
					}else if(pricePercentage >= 1.5) {
						subject = pair + "永续合约价格大涨";
					}
				} else {
					if(pricePercentage >= 15) {
						subject = pair + "永续合约价格大暴涨";
					} else if(pricePercentage >= 10) {
						subject = pair + "永续合约价格暴涨";
					}else if(pricePercentage >= 5) {
						subject = pair + "永续合约价格大涨";
					}
				}
			}
			
			if(StringUtil.isNotEmpty(subject)) {
				
				subject += percentageStr + "% " + dateStr;
				
				text = subject;
				
				String recEmail = userDetailsService.getRiseAndFallMonitorUserEmail();
				
				sendEmail(subject, text, recEmail);
			}
		}
	}
	
	@Override
	public void futuresConsolidationAreaMonitor(List<Klines> klinesList,List<Klines> hitKlinesList) {
		Klines last = PriceUtil.getLastKlines(klinesList);
		String pair = last.getPair();
		
		Klines current = PriceUtil.getLastKlines(hitKlinesList);
		double closePrice = current.getClosePriceDoubleValue();
		
		FibUtil_v3 fu = new FibUtil_v3(klinesList);
		FibInfo firstFibInfo = fu.getFibInfo();
		FibInfo secondFibInfo = fu.getSecondFibInfo(firstFibInfo);
		FibInfo thirdFibInfo = fu.getThirdFibInfo(secondFibInfo);
		FibInfo fourthFibInfo = fu.getFourthFibInfo(thirdFibInfo);
		
		String subject = "";
		String text = "";
		String dateStr = DateFormatUtil.format(new Date());
		
		if(fourthFibInfo != null) {
			double t_price = thirdFibInfo.getFibValue(FibCode.FIB1);
			double f_price = fourthFibInfo.getFibValue(FibCode.FIB1);
			double areaHighPrice = PriceUtil.getMaxPrice(t_price, f_price);
			double areaLowPrice = PriceUtil.getMinPrice(t_price, f_price);
			
			if(PriceUtil.isBreachShort(current, areaHighPrice)) { //高点做空
				String recEmail = userDetailsService.getAreaMonitorUserEmail();
				
				subject = String.format("%s永续合约盘整区高点(%s)做空交易机会 %s", pair, areaHighPrice, dateStr);
				text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						pair,
						PriceUtil.formatDoubleDecimal(areaLowPrice,current.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(areaHighPrice,current.getDecimalNum()),
						closePrice);
				sendEmail(subject, text, recEmail);
				
				FibInfo fibInfo = new FibInfo(areaLowPrice, areaHighPrice, current.getDecimalNum(), FibLevel.LEVEL_1);
				marketPlace(pair, PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.AREA_INDEX);
			}
			
			if(PriceUtil.isBreachLong(current, areaLowPrice)) {
				String recEmail = userDetailsService.getAreaMonitorUserEmail();
				
				subject = String.format("%s永续合约盘整区低点(%s)做多交易机会 %s", pair, areaLowPrice, dateStr);
				text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						pair,
						PriceUtil.formatDoubleDecimal(areaLowPrice,current.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(areaHighPrice,current.getDecimalNum()),
						closePrice);
				sendEmail(subject, text, recEmail);
				
				FibInfo fibInfo = new FibInfo(areaHighPrice, areaLowPrice, current.getDecimalNum(), FibLevel.LEVEL_1);
				marketPlace(pair, PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.AREA_INDEX);
			}
		}
	}
	
	@Override
	public void horizontalRay(Klines klines, ShapeInfo info) {
		//价格坐标
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		if(pointsJsonArray.length() > 0) {
			JSONObject points = pointsJsonArray.getJSONObject(0);
			double price = points.getDouble("price");
			long time = points.getLong("time");
			//double createPrice = info.getPriceDoubleValue();
			
			String dateStr = DateFormatUtil.format(new Date());
			String subject = String.format("%s永续合约价格已到达%s %s", klines.getPair(), PriceUtil.formatDoubleDecimal(price,klines.getDecimalNum()),dateStr);
			String text = String.format("%s永续合约水平射线价格坐标：%s，水平射线时间坐标：%s，当前价格：%s", 
					klines.getPair(),PriceUtil.formatDoubleDecimal(price,klines.getDecimalNum()),
					DateFormatUtil.format(time * 1000),klines.getClosePrice());
			
			if(hitPrice(klines, price)) {
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));
				
				//所有k线信息
				List<Klines> list = klinesRepository.findByPairAndGtStartTime(info.getSymbol(), time * 1000, Inerval.INERVAL_15M);
				if(!CollectionUtils.isEmpty(list)) {
					Klines high = PriceUtil.getMaxPriceKLine(list);
					Klines low = PriceUtil.getMinPriceKLine(list);
					
					double highValue = klines.getHighPriceDoubleValue() > high.getHighPriceDoubleValue() ? klines.getHighPriceDoubleValue() : high.getHighPriceDoubleValue();
					double lowValue = klines.getLowPriceDoubleValue() < low.getLowPriceDoubleValue() ? klines.getLowPriceDoubleValue() : low.getLowPriceDoubleValue();
					
					LongOrShortType type = LongOrShortType.resolve(info.getLongOrShortType());
					
					if(type == LongOrShortType.SHORT) {//做空
						FibInfo fibInfo = new FibInfo(lowValue, highValue, klines.getDecimalNum(), FibLevel.LEVEL_1);
						logger.debug(fibInfo);
						marketPlace(info.getSymbol(), PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
					}
					
					if(type == LongOrShortType.LONG) { // 做多
						FibInfo fibInfo = new FibInfo(highValue, lowValue, klines.getDecimalNum(), FibLevel.LEVEL_1);
						logger.debug(fibInfo);
						marketPlace(info.getSymbol(), PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
					}
				}
			}
			
		}
	}
	
	/**
	 * 判断价格是否到达预期的价格
	 * @param klines 当前k线
	 * @param price 预期的价格
	 * @return
	 */
	private boolean hitPrice(Klines klines,double price) {
		boolean result = false;
		if(Double.valueOf(klines.getHighPrice()) >= price && Double.valueOf(klines.getLowPrice()) <= price) {
			result = true;
		}
		return result;
	}

	@Override
	public void rectangle(Klines klines, ShapeInfo info) {
		//价格坐标
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		if(pointsJsonArray.length() > 1) {
			JSONObject points = pointsJsonArray.getJSONObject(0);
			double price0 = points.getDouble("price");
			//long time0 = points.getLong("time");
			
			JSONObject points1 = pointsJsonArray.getJSONObject(1);
			double price1 = points1.getDouble("price");
			//long time1 = points1.getLong("time");
			
			String upOrLowStr = "";
			
			if(hitPrice(klines, price0)) {
				upOrLowStr = price0 > price1 ? "上" : "下";
				String dateStr = DateFormatUtil.format(new Date());
				String subject = String.format("%s永续合约价格已到达盘整区%s边缘%s %s", klines.getPair(), upOrLowStr, PriceUtil.formatDoubleDecimal(price0,klines.getDecimalNum()),dateStr);
				String text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price0,klines.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(price1,klines.getDecimalNum()),
						klines.getClosePrice());
				
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));

				FibInfo fibInfo = new FibInfo(price1, price0, klines.getDecimalNum(), FibLevel.LEVEL_1);
				if(upOrLowStr.equals("上")) {//做空
					marketPlace(info.getSymbol(), PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				} else {//做多
					marketPlace(info.getSymbol(), PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				}
			}
			
			if(hitPrice(klines, price1)) {
				upOrLowStr = price1 > price0 ? "上" : "下";
				String dateStr = DateFormatUtil.format(new Date());
				String subject = String.format("%s永续合约价格已到达盘整区%s边缘%s %s", klines.getPair(), upOrLowStr, PriceUtil.formatDoubleDecimal(price1,klines.getDecimalNum()),dateStr);
				String text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price0,klines.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(price1,klines.getDecimalNum()),
						klines.getClosePrice());
				
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));
				
				FibInfo fibInfo = new FibInfo(price0, price1, klines.getDecimalNum(), FibLevel.LEVEL_1);
				if(upOrLowStr.equals("上")) {//做空
					marketPlace(info.getSymbol(), PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				} else {//做多
					marketPlace(info.getSymbol(), PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				}
			}
			
		}
	}
	
	@Override
	public void ray(Klines klines, ShapeInfo info) {
		//价格坐标
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		if(pointsJsonArray.length() > 1) {
			JSONObject points = pointsJsonArray.getJSONObject(0);
			double price0 = points.getDouble("price");
			long time0 = points.getLong("time");
			
			JSONObject points1 = pointsJsonArray.getJSONObject(1);
			double price1 = points1.getDouble("price");
			long time1 = points1.getLong("time");
			
			StraightLineUtil util = new StraightLineUtil(time0 * 1000, price0, time1 * 1000, price1);
			double resultPrice = util.calculateLineYvalue(klines.getStartTime());
			
			String dateStr = DateFormatUtil.format(new Date());
			String subject = String.format("%s永续合约价格已到达趋势线价格%s附近 %s", klines.getPair(), PriceUtil.formatDoubleDecimal(resultPrice,klines.getDecimalNum()),dateStr);
			String text = String.format("%s永续合约射线价格坐标1：%s，时间坐标1：%s，价格坐标2：%s，时间坐标2：%s，当前价格：%s，趋势线价格：%s", 
					klines.getPair(),
					PriceUtil.formatDoubleDecimal(price0,klines.getDecimalNum()),
					DateFormatUtil.format(time0 * 1000),
					PriceUtil.formatDoubleDecimal(price1,klines.getDecimalNum()),
					DateFormatUtil.format(time1 * 1000),
					klines.getClosePrice(),
					PriceUtil.formatDoubleDecimal(resultPrice,klines.getDecimalNum()));
			logger.debug(text);
			if(hitPrice(klines, resultPrice)) {
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));
				
				long startTime = time0 < time1 ? (time0 * 1000) : (time1 * 1000);
				long endTime = time0 < time1 ? (time1 * 1000) : (time0 * 1000);
				
				List<Klines> list = klinesRepository.findByPairAndGtStartTime(info.getSymbol(), endTime, Inerval.INERVAL_15M);
				List<Klines> list_draw = klinesRepository.findByTimeLimit(info.getSymbol(), Inerval.INERVAL_15M, startTime, endTime);
				
				Klines high_draw =  PriceUtil.getMaxPriceKLine(list_draw);
				Klines high = PriceUtil.getMaxPriceKLine(list);
				Klines low = PriceUtil.getMinPriceKLine(list);
				
				double high_draw_value = high_draw.getHighPriceDoubleValue();
				if(high_draw_value > price0 && high_draw_value > price1) { //做多
					List<Klines> fib_klines_list = PriceUtil.subList(high, list);
					Klines fibLow = PriceUtil.getMinPriceKLine(fib_klines_list);
					double highValue = high.getHighPriceDoubleValue();
					double lowValue = fibLow.getLowPriceDoubleValue();
					FibInfo fibInfo = new FibInfo(highValue, lowValue, klines.getDecimalNum(), FibLevel.LEVEL_1);
					marketPlace(info.getSymbol(), PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				} else {//做空
					List<Klines> fib_klines_list = PriceUtil.subList(low, list);
					Klines fibHigh = PriceUtil.getMaxPriceKLine(fib_klines_list);
					double lowValue = low.getLowPriceDoubleValue();
					double highValue = fibHigh.getHighPriceDoubleValue();
					FibInfo fibInfo = new FibInfo(lowValue, highValue, klines.getDecimalNum(), FibLevel.LEVEL_1);
					marketPlace(info.getSymbol(), PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				}
			}
		}
	}

	@Override
	public void parallelChannel(Klines klines, ShapeInfo info) {
		//价格坐标
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		if(pointsJsonArray.length() > 2) {
			JSONObject points = pointsJsonArray.getJSONObject(0);
			double price0 = points.getDouble("price");
			long time0 = points.getLong("time");
			
			JSONObject points1 = pointsJsonArray.getJSONObject(1);
			double price1 = points1.getDouble("price");
			long time1 = points1.getLong("time");
			
			JSONObject points2 = pointsJsonArray.getJSONObject(2);
			double price2 = points2.getDouble("price");
			long time2 = points2.getLong("time");
			
			StraightLineUtil util = new StraightLineUtil(time0 * 1000, price0, time1 * 1000, price1, time2 * 1000, price2);
			
			double line0Price = util.calculateLineYvalue(klines.getStartTime());
			double line1Price = util.calculateLineYvalueForb2(klines.getStartTime());
			
			String upOrLowStr = "";
			
			logger.debug(String.format("价格1：%s，价格2：%s，时间：%s", line0Price,line1Price,DateFormatUtil.format(klines.getStartTime())));
			
			//第一条直线
			if(hitPrice(klines, line0Price)) {
				upOrLowStr = line0Price > line1Price ? "上" : "下";
				String dateStr = DateFormatUtil.format(new Date());
				String subject = String.format("%s永续合约价格已到达平行通道%s边缘%s %s", klines.getPair(), upOrLowStr, PriceUtil.formatDoubleDecimal(line0Price,klines.getDecimalNum()),dateStr);
				String text = String.format("%s永续合约平行通道价格坐标1：%s，时间坐标1：%s，价格坐标2：%s，时间坐标2：%s，价格坐标3：%s，时间坐标3：%s，，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price0,klines.getDecimalNum()),
						DateFormatUtil.format(time0 * 1000),
						PriceUtil.formatDoubleDecimal(price1,klines.getDecimalNum()),
						DateFormatUtil.format(time1 * 1000),
						PriceUtil.formatDoubleDecimal(price2,klines.getDecimalNum()),
						DateFormatUtil.format(time2 * 1000),
						klines.getClosePrice());
				
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));
				
				FibInfo fibInfo = new FibInfo(line1Price, line0Price, klines.getDecimalNum(), FibLevel.LEVEL_1);
				
				if(upOrLowStr.equals("上")) { //做空
					marketPlace(info.getSymbol(), PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				} else { //做多
					marketPlace(info.getSymbol(), PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				}
			}
			
			//第二条直线
			if(hitPrice(klines, line1Price)) {
				upOrLowStr = line1Price > line0Price ? "上" : "下";
				String dateStr = DateFormatUtil.format(new Date());
				String subject = String.format("%s永续合约价格已到达平行通道%s边缘%s %s", klines.getPair(), upOrLowStr, PriceUtil.formatDoubleDecimal(line1Price,klines.getDecimalNum()),dateStr);
				String text = String.format("%s永续合约平行通道价格坐标1：%s，时间坐标1：%s，价格坐标2：%s，时间坐标2：%s，价格坐标3：%s，时间坐标3：%s，，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price0,klines.getDecimalNum()),
						DateFormatUtil.format(time0 * 1000),
						PriceUtil.formatDoubleDecimal(price1,klines.getDecimalNum()),
						DateFormatUtil.format(time1 * 1000),
						PriceUtil.formatDoubleDecimal(price2,klines.getDecimalNum()),
						DateFormatUtil.format(time2 * 1000),
						klines.getClosePrice());
				
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));
				
				FibInfo fibInfo = new FibInfo(line0Price, line1Price, klines.getDecimalNum(), FibLevel.LEVEL_1);
				
				if(upOrLowStr.equals("上")) { //做空
					marketPlace(info.getSymbol(), PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				} else { //做多
					marketPlace(info.getSymbol(), PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				}
			}
		}
	}

	@Override
	public void trianglePattern(Klines klines, ShapeInfo info) {
		String dateStr = DateFormatUtil.format(new Date());
		//价格坐标 A B C D 四个点
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		if(pointsJsonArray.length() > 3) {
			JSONObject points = pointsJsonArray.getJSONObject(0);
			//A点坐标
			double price0 = points.getDouble("price");
			long time0 = points.getLong("time");
			
			//B点坐标
			JSONObject points1 = pointsJsonArray.getJSONObject(1);
			double price1 = points1.getDouble("price");
			long time1 = points1.getLong("time");
			
			//C点坐标
			JSONObject points2 = pointsJsonArray.getJSONObject(2);
			double price2 = points2.getDouble("price");
			long time2 = points2.getLong("time");
			
			//D点坐标
			JSONObject points3 = pointsJsonArray.getJSONObject(3);
			double price3 = points3.getDouble("price");
			long time3 = points3.getLong("time");
			
			//初始化AC、BD两条直线
			StraightLineUtil util = new StraightLineUtil(time0 * 1000, price0, time1 * 1000, price1, time2 * 1000, price2, time3 * 1000, price3);
			

			String parallelChannelOrTrianglePattern = "";
			String upOrLowStr = "";
			
			//相交或平行两种情况
			//相交
			if(util.isIntersect()) {
				parallelChannelOrTrianglePattern = "三角形";
				//两条直线相交的时间
				long intersectTime = util.getIntersectXValue();
				logger.debug(String.format("三角形AC和BD两条直线相交时间：%s", DateFormatUtil.format(intersectTime)));
				//正常绘图 相交时间应在ABCD四个点之后 否则不做分析
				if(!(intersectTime > time0 && intersectTime > time1 && intersectTime > time2 && intersectTime > time3)) {
					return;
				}
				//如果k线在相交时间之后则不做分析
				if(klines.getStartTime() > intersectTime) {
					return;
				}
				
			} else {//平行
				parallelChannelOrTrianglePattern = "平行通道";
				logger.debug("AC与BD两条直线平行，绘图分析为平行通道");
			}
			
			//两条直线当前对应的价格
			//第一条直线 AC
			double acPrice = util.calculateLineYvalue(klines.getStartTime());
			//第二条直线 BD
			double bdPrice = util.calculateLine2Yvalue(klines.getStartTime());
			
			logger.debug(String.format("AC价格：%s，BD价格：%s，当前价格：%s", 
					PriceUtil.formatDoubleDecimal(acPrice,klines.getDecimalNum()),
					PriceUtil.formatDoubleDecimal(bdPrice,klines.getDecimalNum()),
					klines.getClosePrice()));
			

			String subject = "";
			String text = "";
			
			FibInfo fibInfo = null;
			
			//k线经过ac直线时
			if(hitPrice(klines, acPrice)) {
				upOrLowStr = acPrice > bdPrice ? "上" : "下";
				subject = String.format("%s永续合约价格到达%s%s边缘%s %s", 
						klines.getPair(), 
						parallelChannelOrTrianglePattern, 
						upOrLowStr,
						PriceUtil.formatDoubleDecimal(acPrice,klines.getDecimalNum()),
						dateStr);
				fibInfo = new FibInfo(bdPrice, acPrice, klines.getDecimalNum(), FibLevel.LEVEL_1);
			}
			
			if(hitPrice(klines, bdPrice)) {
				upOrLowStr = bdPrice > acPrice ? "上" : "下";
				subject = String.format("%s永续合约价格到达%s%s边缘%s %s", 
						klines.getPair(), 
						parallelChannelOrTrianglePattern, 
						upOrLowStr,
						PriceUtil.formatDoubleDecimal(bdPrice,klines.getDecimalNum()),
						dateStr);
				fibInfo = new FibInfo(acPrice, bdPrice, klines.getDecimalNum(), FibLevel.LEVEL_1);
			}
			
			text = String.format("%s永续合约%sA点时间坐标：%s，A点价格坐标：%s，B点时间坐标：%s，B点价格坐标：%s，C点时间坐标：%s，C点价格坐标：%s，D点时间坐标：%s，D点价格坐标：%s"
					+ "，当前价格：%s", 
					klines.getPair(),
					parallelChannelOrTrianglePattern,
					DateFormatUtil.format(time0 * 1000),
					PriceUtil.formatDoubleDecimal(price0, klines.getDecimalNum()),
					DateFormatUtil.format(time1 * 1000),
					PriceUtil.formatDoubleDecimal(price1, klines.getDecimalNum()),
					DateFormatUtil.format(time2 * 1000),
					PriceUtil.formatDoubleDecimal(price2, klines.getDecimalNum()),
					DateFormatUtil.format(time3 * 1000),
					PriceUtil.formatDoubleDecimal(price3, klines.getDecimalNum()),
					klines.getClosePrice()
					);
			
			if(StringUtil.isNotEmpty(subject)) {
				
				this.emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));
				
				if(upOrLowStr.equals("上")) {//做空
					marketPlace(info.getSymbol(), PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				} else {
					marketPlace(info.getSymbol(), PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.DEFAULT);
				}
			}
		}
	}

	@Override
	public void riskRewardLong(Klines klines, ShapeInfo info) {
		//价格坐标
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		JSONObject propertiesJson = new JSONObject(info.getProperties());
		if(pointsJsonArray.length() > 0) {
			
			String dateStr = DateFormatUtil.format(new Date());
			
			JSONObject points = pointsJsonArray.getJSONObject(0);
			
			double price = points.getDouble("price");//开仓价
			//long time = points.getLong("time");
			
			double n = 100 * Math.pow(10, klines.getDecimalNum() - 2);
			double stopLevel = propertiesJson.getDouble("stopLevel") / n;//止损金额
			double profitLevel = propertiesJson.getDouble("profitLevel") / n;//止盈金额
			
			String pair = klines.getPair();
			
			if(hitPrice(klines, price)) {
				String subject = String.format("%s永续合约(%s)做多交易计划 %s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price, klines.getDecimalNum()),
						dateStr);
				
				double stopLossDoubleValue = price - stopLevel;
				double takeProfitDoubleValue = price + profitLevel;
				
				String text = StringUtil.formatLongMessage(klines.getPair(), price, stopLossDoubleValue, takeProfitDoubleValue, klines.getDecimalNum());
				this.emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));
				
				marketPlace(pair, PositionSide.LONG, stopLossDoubleValue, takeProfitDoubleValue, 0, null, AutoTradeType.DEFAULT);
				
				shapeRepository.deleteById(info.getId());
			}
			
		}
	}

	@Override
	public void riskRewardShort(Klines klines, ShapeInfo info) {
		//价格坐标
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		JSONObject propertiesJson = new JSONObject(info.getProperties());
		if(pointsJsonArray.length() > 0) {
			
			String dateStr = DateFormatUtil.format(new Date());
			
			JSONObject points = pointsJsonArray.getJSONObject(0);
			
			double price = points.getDouble("price");//开仓价
			//long time = points.getLong("time");
			
			double n = 100 * Math.pow(10, klines.getDecimalNum() - 2);
			double stopLevel = propertiesJson.getDouble("stopLevel") / n;//止损金额
			double profitLevel = propertiesJson.getDouble("profitLevel") / n;//止盈金额
			
			String pair = klines.getPair();
			
			if(hitPrice(klines, price)) {
				String subject = String.format("%s永续合约(%s)做空交易计划 %s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price, klines.getDecimalNum()),
						dateStr);
				
				double stopLossDoubleValue = price + stopLevel;
				double takeProfitDoubleValue = price - profitLevel;
				
				String text = StringUtil.formatLongMessage(klines.getPair(), price, stopLossDoubleValue, takeProfitDoubleValue, klines.getDecimalNum());
				this.emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));
				
				marketPlace(pair, PositionSide.SHORT, stopLossDoubleValue, takeProfitDoubleValue, 0, null, AutoTradeType.DEFAULT);
				
				shapeRepository.deleteById(info.getId());
			}
			
		}
	}

	@Override
	public void fibRetracement(Klines klines, ShapeInfo info) {
		//价格坐标
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		JSONObject propertiesJson = new JSONObject(info.getProperties());
		if(pointsJsonArray.length() > 1) {
			
			String dateStr = DateFormatUtil.format(new Date());
			
			JSONObject points = pointsJsonArray.getJSONObject(0);
			JSONObject points2 = pointsJsonArray.getJSONObject(1);
			double fib1Price = points.getDouble("price");
			double fib0Price = points2.getDouble("price");
			
			FibInfo fib = new FibInfo(fib1Price, fib0Price, klines.getDecimalNum(), FibLevel.LEVEL_1);
			
			for(int index = 0;index < 12;index++) {
				JSONObject levelObj = propertiesJson.getJSONObject("level" + (index + 1));
				boolean visible = levelObj.getBoolean("visible");
				double coeff = levelObj.getDouble("coeff");
				double price = fib.getFibValue(coeff);
				if(visible && hitPrice(klines, price)) {
					String subject = String.format("%s永续合约价格已到达%s(%s) %s",
							klines.getPair(),
							coeff,
							PriceUtil.formatDoubleDecimal(price, klines.getDecimalNum()),
							dateStr);
					String text = fib.toString();
					this.emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner(), emailRepository));
				}
			}
		}
	}
	
	@Override
    public boolean checkData(List<Klines> list) {
        boolean result = true;
        if(!CollectionUtils.isEmpty(list)){
            list.sort(new KlinesComparator(SortType.ASC));
            
            long parentSubTime = 0;
            
            for(int index = 0;index < list.size();index++){
                if(index == list.size() - 1){
                    continue;
                }
                Klines current = list.get(index);
                Klines next = list.get(index + 1);

                long currentSubTime = next.getStartTime() - current.getEndTime();
                
                if(parentSubTime == 0) {
                    parentSubTime = currentSubTime;
                }
                
                long startTime = 0;
                long endTime = 0;
                //前两根k线之间有缺失数据
                if(parentSubTime > currentSubTime) {
                	
                	Klines parent = list.get(index - 1);
                	startTime = parent.getEndTime();
                	endTime = current.getStartTime();
                	
                }//后两根k线之间有缺失数据
                else if(parentSubTime < currentSubTime) {
                	
                	startTime = current.getEndTime();
                	endTime = next.getStartTime();
                }
                
                if(startTime < endTime && endTime - startTime > 60000) {
                	//用来标记原本缺陷的k线
                	String fileName = current.getPair() + "_" + current.getInterval() + "_" + startTime + "_" + endTime + ".defect";
                	if(!FileUtil.exists(fileName)) {
                    	result = false;
                    	List<Klines> data = continuousKlines(current.getPair(), startTime, endTime, current.getInervalType(), QUERY_SPLIT.NOT_ENDTIME);
                    	logger.info(current.getPair() + "交易对" + current.getInterval() + "级别k线信息数据有缺矢，已同步" + data.size() 
                    				+ "条数据，缺失时间段：" + DateFormatUtil.format(startTime) + " ~ " + DateFormatUtil.format(endTime));
                    	klinesRepository.insert(data);
                    	//创建同步标识
                    	FileUtil.createFile(fileName);
                	} else {
                		logger.info("{}交易对{}级别{}~{}缺失部分已是最新数据", current.getPair(), current.getInterval(), 
                				DateFormatUtil.format(startTime), DateFormatUtil.format(endTime));
                	}
                }
            }
            
            for(int index = 0;index < list.size();index++){
                if(index == list.size() - 1){
                    continue;
                }
                Klines current = list.get(index);
                Klines next = list.get(index + 1);
                
                //判断重复
                if(current.getStartTime() == next.getStartTime()){
                    logger.info("查询到重复K线信息：" + current);
                    result = false;
                    String _id = current.getId();
                    if(StringUtil.isNotEmpty(_id)){
                    	klinesRepository.remove(_id);
                        logger.info("重复k线已从数据库中删除");
                    }
                }
            }
            
        }
        return result;
    }

	@Override
	public String getClosePrice(String pair, Inerval inerval) {
		String price = "0";
		List<Klines> list = klinesRepository.findLastKlinesByPair(pair, inerval, 1);
		if(!CollectionUtils.isEmpty(list)) {
			price = list.get(0).getClosePrice();
		}
		return price;
	}

	@Override
	public boolean verifyUpdateDayKlines(List<Klines> list) {
		Date now = new Date();
		//开盘时间不校验更新
		if(DateFormatUtil.verifyLastDayStartTime(now)) {
			return false;
		}
		if(!CollectionUtils.isEmpty(list)) {
			//最后一天k线
			Klines last = PriceUtil.getLastKlines(list);
			//校验是否为日线级别
			if(!last.verifyInterval(Inerval.INERVAL_1D)) {
				return false;
			}
			//如果最后一根k线为最后一天k线则不更新
			if(PriceUtil.verifyLastDay(last)) {
				return false;
			}
			//开始执行更新逻辑
			//获取需要更新的时间段信息
			long startTime = DateFormatUtil.parse(DateFormatUtil.format(last.getEndTime())).getTime() + 1000;
			long endTime = DateFormatUtil.getEndTime(DateFormatUtil.getHours(now.getTime())).getTime();
			List<Klines> list_day = this.continuousKlines(last.getPair(), startTime, endTime, Inerval.INERVAL_1D, QUERY_SPLIT.NOT_ENDTIME);
			if(CollectionUtils.isEmpty(list_day)) {
				String message = "未同步到时间段" + DateFormatUtil.format(startTime) + "~" + DateFormatUtil.format(endTime) + last.getPair() + "交易对日线级别K线信息";
				throw new RuntimeException(message);
			}
			
			logger.info("已获取到时间段" + DateFormatUtil.format(startTime) + "~" + DateFormatUtil.format(endTime) + last.getPair() + "交易对日线级别" + list_day.size() + "条K线信息");
			klinesRepository.insert(list_day);
			
			return true;
		}
		return false;
	}

	@Override
	public void volumeMonitor(List<Klines> list) {
		if(CollectionUtils.isEmpty(list) || list.size() < 3) {
			return;
		}
		
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		String pair = k0.getPair();
		
		String subject = "";
		String text = k0.toString();
		
		String dateStr = DateFormatUtil.format(new Date());
		
		if(PriceUtil.isFall_v3(list) && PriceUtil.isRelease(list)) {//下跌
			
			if(k0.isDownlead() && !k0.isUplead()) {
				subject = String.format("%s永续合约出现买盘 %s", pair, dateStr);
			} else {
				subject = String.format("%s永续合约放量下跌 %s", pair, dateStr);
			}
			
		} else if(PriceUtil.isRise_v3(list) && PriceUtil.isRelease(list)) { //上涨
			
			if(k0.isUplead() && !k0.isDownlead()) {
				subject = String.format("%s永续合约出现抛压 %s", pair, dateStr);
			} else {
				subject = String.format("%s永续合约放量上涨 %s", pair, dateStr);
			}
			
		}
		
		if(StringUtil.isNotEmpty(subject)) {
			String recEmail = userDetailsService.getVolumeMonitorUserEmail();
			sendEmail(subject, text, recEmail);
		}
	}
}
