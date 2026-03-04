package com.bugbycode.service.klines.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
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

import com.bugbycode.binance.module.commission_rate.CommissionRate;
import com.bugbycode.binance.module.leverage.LeverageBracketInfo;
import com.bugbycode.binance.module.position.PositionInfo;
import com.bugbycode.binance.trade.rest.BinanceRestTradeService;
import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.exception.OrderPlaceException;
import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.factory.area.impl.AreaFactoryImpl;
import com.bugbycode.factory.fibInfo.FibInfoFactory;
import com.bugbycode.factory.fibInfo.impl.FibInfoFactoryImpl_v2;
import com.bugbycode.factory.priceAction.PriceActionFactory;
import com.bugbycode.factory.priceAction.impl.PriceActionFactoryImpl;
import com.bugbycode.module.BreakthroughTradeStatus;
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
import com.bugbycode.module.VolumeMonitorStatus;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.CallbackRateEnabled;
import com.bugbycode.module.binance.ContractType;
import com.bugbycode.module.binance.DrawTrade;
import com.bugbycode.module.binance.MarginType;
import com.bugbycode.module.binance.PriceInfo;
import com.bugbycode.module.binance.ProfitOrderEnabled;
import com.bugbycode.module.binance.Result;
import com.bugbycode.module.binance.SymbolConfig;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.module.open_interest.OpenInterestHist;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.user.User;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;
import com.bugbycode.repository.shape.ShapeRepository;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.service.user.UserService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.email.SendMailTask;
import com.bugbycode.trading_app.task.trading.TradingTask;
import com.util.CommandUtil;
import com.util.DateFormatUtil;
import com.util.FileUtil;
import com.util.KlinesComparator;
import com.util.LeverageBracketUtil;
import com.util.PairPolicyUtil;
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
	private WorkTaskPool tradingTaskPool;
	
	@Autowired
	private KlinesRepository klinesRepository;
	
	@Autowired
	private BinanceRestTradeService binanceRestTradeService;
	
	@Autowired
	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	@Autowired
	private ShapeRepository shapeRepository;
	
	@Autowired
	private OpenInterestHistRepository openInterestHistRepository;
	
	@Override
	public List<Klines> continuousKlines(String pair, long startTime, long endTime,
			Inerval interval,QUERY_SPLIT split, ContractType contractType) {
		
		SymbolExchangeInfo info = AppConfig.SYMBOL_EXCHANGE_INFO.get(pair);
		if(info == null) {
			throw new RuntimeException("交易规则和交易对信息未初始化");
		}
		
		String baseUrl = AppConfig.REST_BASE_URL + "/fapi/v1/continuousKlines";
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
				.queryParam("pair", pair)
				.queryParam("contractType", info.getContractType().getValue())
				.queryParam("startTime", startTime)
				.queryParam("interval", interval.getDescption())
				.queryParam("limit", 1500);
		if(contractType == ContractType.E_OPTIONS) {
			baseUrl = AppConfig.EOPTIONS_BASE_URL + "/eapi/v1/klines";
			uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
					.queryParam("symbol", pair)
					//.queryParam("contractType", info.getContractType().getValue())
					.queryParam("startTime", startTime)
					.queryParam("interval", interval.getDescption())
					.queryParam("limit", 1500);
		}
		
		
		switch (split) {
		case NOT_ENDTIME:
			
			uriBuilder.queryParam("endTime", endTime);
			
			break;

		default:
			break;
		}
		
		String url = uriBuilder.toUriString();
		
		try {
			url = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
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
	public List<Klines> continuousKlines1Day(String pair, Date now, int limit,QUERY_SPLIT split, ContractType contractType) {
		
		int hours = DateFormatUtil.getHours(now.getTime());
		Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
		Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
		
		Date firstDayStartTime = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -limit);//多少天以前起始时间
		
		return continuousKlines(pair, firstDayStartTime.getTime(), 
				lastDayEndTimeDate.getTime() + 999, Inerval.INERVAL_1D,split, contractType);
	}

	@Override
	public List<Klines> continuousKlines5M(String pair, Date now, int limit,QUERY_SPLIT split, ContractType contractType) {
		List<Klines> result = null;
		try {
			
			Date endTime_5m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
			Date startTime_5m = DateFormatUtil.getStartTimeBySetMinute(endTime_5m, -Inerval.INERVAL_5M.getNumber() * limit);//limit根k线
			endTime_5m = DateFormatUtil.getStartTimeBySetMillisecond(endTime_5m, -1);//收盘时间
			
			result = continuousKlines(pair, startTime_5m.getTime(),
					endTime_5m.getTime(), Inerval.INERVAL_5M,split, contractType);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public List<Klines> continuousKlines15M(String pair, Date now, int limit,QUERY_SPLIT split, ContractType contractType) {
		List<Klines> result = null;
		try {
			
			Date endTime = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
			Date startTime = DateFormatUtil.getStartTimeBySetMinute(endTime, -Inerval.INERVAL_15M.getNumber() * limit);//limit根k线
			endTime = DateFormatUtil.getStartTimeBySetMillisecond(endTime, -1);//收盘时间
			
			result = continuousKlines(pair, startTime.getTime(),
					endTime.getTime(), Inerval.INERVAL_15M,split,contractType);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@Override
	public List<Klines> continuousKlines4H(String pair,Date now,int limit,QUERY_SPLIT split, ContractType contractType) {
		List<Klines> result = null;
		try {
			
			Date endTime = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_00_00(now));
			Date startTime = DateFormatUtil.getStartTimeBySetHour(endTime, -Inerval.INERVAL_4H.getNumber() * 4 * limit);//limit根k线
			endTime = DateFormatUtil.getStartTimeBySetMillisecond(endTime, -1);//收盘时间
			
			result = continuousKlines(pair, startTime.getTime(),
					endTime.getTime(), Inerval.INERVAL_4H,split,contractType);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public void openLong_v2(List<OpenPrice> openPrices, FibInfo fibInfo, Klines afterLowKlines,
			List<Klines> klinesList_hit) {
		if(fibInfo == null) {
			return;
		}
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		String pair = hitKline.getPair();
		
		OpenInterestHist oih = openInterestHistRepository.findOneBySymbol(pair);
		
		//收盘价格
		double closePrice = hitKline.getClosePriceDoubleValue();
		double currentPrice = closePrice;
		
		for(int index = 0;index < openPrices.size(); index++) {
		
			OpenPrice openPrice = openPrices.get(index);
			double price = openPrice.getPrice();
			
			FibCode code = openPrice.getCode();//当前斐波那契点位
			
			if(PriceUtil.isBreachLong(hitKline, price)
					&& !PriceUtil.isObsoleteLong(afterLowKlines, openPrices, index)
					&& !PriceUtil.isTraded(price, fibInfo)
					&& fibInfo.verifyOpenPrice(openPrice, currentPrice)
					) {
			
				//市价做多
				this.tradingTaskPool.add(new TradingTask(this, pair, PositionSide.LONG, 0, 0, openPrice, fibInfo, AutoTradeType.FIB_RET, fibInfo.getDecimalPoint()));
				
				//
				List<User> userList = userRepository.queryAllUserByFibMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					if(!PairPolicyUtil.verifyPairPolicy(u.getPairPolicySelected(), pair, u.getMonitorPolicyType())) {
						continue;
					}
					
					if(oih.getTradeNumber() < u.getTradeNumberMonitor()) {
						continue;
					}
					
					if(code.lt(u.getMonitorFibLevelType().getLevelCode())) {
						continue;
					}
					
					//回踩单判断
					TradeStepBackStatus tradeStepBackStatus = TradeStepBackStatus.valueOf(u.getTradeStepBack());
					
					if(code.gt(FibCode.FIB1) && tradeStepBackStatus == TradeStepBackStatus.CLOSE) {
						continue;
					}
					
					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					FibCode closePpositionCode = fibInfo.getTakeProfit_v2(code);//止盈点位
					
					//保守的交易风格
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						closePpositionCode = fibInfo.getTakeProfit_v3(code, currentPrice, u.getMonitorProfit(), u.getProfitLimit());
					}
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getRiseFluctuationPercentage(currentPrice, fibInfo.getFibValue(closePpositionCode)) * 100;
					
					if(profitPercent < u.getMonitorProfit()) {
						continue;
					}
					
					//止盈价
					double profitPrice = fibInfo.getFibValue(closePpositionCode);

					//开仓订阅提醒
					String subject = String.format("%s永续合约%s(%s)[%s]做多机会(PNL:%s%%) %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatLongMessage(pair, currentPrice, PriceUtil.rectificationCutLossLongPrice_v3(currentPrice, u.getCutLoss()), 
							profitPrice, fibInfo.getDecimalPoint());
					
					text += "\r\n\r\n" + fibInfo.toString();
					
					sendEmail(u, subject, text, u.getUsername());
				}
				break;
			}
		}
	}

	@Override
	public void openShort_v2(List<OpenPrice> openPrices, FibInfo fibInfo, Klines afterHighKlines,
			List<Klines> klinesList_hit) {
		if(fibInfo == null) {
			return;
		}
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		
		//收盘价格
		double closePrice = hitKline.getClosePriceDoubleValue();
		double currentPrice = closePrice;
		String pair = hitKline.getPair();
		
		OpenInterestHist oih = openInterestHistRepository.findOneBySymbol(pair);
		
		for(int index = 0;index < openPrices.size(); index++) {
			
			OpenPrice openPrice = openPrices.get(index);
			double price = openPrice.getPrice();
			
			FibCode code = openPrice.getCode();//当前斐波那契点位
			
			if(PriceUtil.isBreachShort(hitKline, price)
					&& !PriceUtil.isObsoleteShort(afterHighKlines, openPrices, index)
					&& !PriceUtil.isTraded(price, fibInfo)
					&& fibInfo.verifyOpenPrice(openPrice, currentPrice)
					) {
			
				//市价做空
				this.tradingTaskPool.add(new TradingTask(this, pair, PositionSide.SHORT, 0, 0, openPrice,  fibInfo, AutoTradeType.FIB_RET, fibInfo.getDecimalPoint()));

				//
				List<User> userList = userRepository.queryAllUserByFibMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					if(!PairPolicyUtil.verifyPairPolicy(u.getPairPolicySelected(), pair, u.getMonitorPolicyType())) {
						continue;
					}
					
					if(oih.getTradeNumber() < u.getTradeNumberMonitor()) {
						continue;
					}
					
					if(code.lt(u.getMonitorFibLevelType().getLevelCode())) {
						continue;
					}

					//回踩单判断
					TradeStepBackStatus tradeStepBackStatus = TradeStepBackStatus.valueOf(u.getTradeStepBack());
					
					if(code.gt(FibCode.FIB1) && tradeStepBackStatus == TradeStepBackStatus.CLOSE) {
						continue;
					}
					
					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					FibCode closePpositionCode = fibInfo.getTakeProfit_v2(code);//止盈点位
					
					//保守的交易风格
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						closePpositionCode = fibInfo.getTakeProfit_v3(code, currentPrice, u.getMonitorProfit(), u.getProfitLimit());
					}
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getFallFluctuationPercentage(currentPrice, fibInfo.getFibValue(closePpositionCode)) * 100;
					
					if(profitPercent < u.getMonitorProfit()) {
						continue;
					}
					
					//止盈价
					double profitPrice = fibInfo.getFibValue(closePpositionCode);
					
					String subject = String.format("%s永续合约%s(%s)[%s]做空机会(PNL:%s%%) %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatShortMessage(pair, currentPrice, profitPrice, 
							PriceUtil.rectificationCutLossShortPrice_v3(currentPrice, u.getCutLoss()), fibInfo.getDecimalPoint());
					
					text += "\r\n\r\n" + fibInfo.toString();
					
					
					sendEmail(u, subject,text, u.getUsername());
				}
				break;
			}
		}
	}

	@SuppressWarnings("null")
	@Override
	public void marketPlace(String pair,PositionSide ps, double stopLossDoubleValue, double takeProfitDoubleValue, OpenPrice openPrice, 
			FibInfo fibInfo, AutoTradeType autoTradeType, int decimalNum) {
		
		OpenInterestHist oih = openInterestHistRepository.findOneBySymbol(pair);
		
		SymbolExchangeInfo info = AppConfig.SYMBOL_EXCHANGE_INFO.get(pair);
		if(info == null) {
			throw new RuntimeException(pair + "交易规则未初始化");
		}
		
		String marginAsset = info.getMarginAsset();

		//只做U本位(USDT)合约
		if(!"USDT".equals(marginAsset)) {
			logger.info(pair + "保证金资产为" + marginAsset);
			return;
		}
		
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
			BreakthroughTradeStatus breakthroughTradeStatus = BreakthroughTradeStatus.valueOf(u.getBreakthroughTrade());
			double callbackRate = u.getCallbackRate();
			double activationPriceRatio = u.getActivationPriceRatio();
			CallbackRateEnabled callbackRateEnabled = CallbackRateEnabled.valueOf(u.getCallbackRateEnabled());
			
			ProfitOrderEnabled profitOrderEnabled = ProfitOrderEnabled.OPEN;
			
			//筛选策略限制
			if(!PairPolicyUtil.verifyPairPolicy(u.getTradePairPolicySelected(), pair, u.getTradePolicyType())) {
				continue;
			}
			
			//活跃度限制
			if(oih.getTradeNumber() < u.getTradeNumber() && autoTradeType != AutoTradeType.DEFAULT) {
				continue;
			}
			//计算预计盈利百分比
			double profitPercent = 0;
			
			BigDecimal callbackRateValue = new BigDecimal(callbackRate);
			BigDecimal stopLoss = new BigDecimal(0);
			BigDecimal takeProfit = new BigDecimal(0);
			
			if(ps == PositionSide.LONG) {//做多
					
				try {
					
					PriceInfo priceInfo = binanceWebsocketTradeService.getPrice(pair);
					
					//计算追踪止损触发价
					BigDecimal activationPriceValue = new BigDecimal(
							PriceUtil.formatDoubleDecimal(PriceUtil.calculateLongActivationPrice(priceInfo.getPriceDoubleValue(), activationPriceRatio), decimalNum)
							);
					
					if(autoTradeType == AutoTradeType.DEFAULT) {//自定义止盈止损
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
							
							FibCode code = openPrice.getCode();
							
							if(code.lt(u.getFibLevelType().getLevelCode())) {
								continue;
							}
							
							takeProfitCode = fibInfo.getTakeProfit_v2(code);
							
							//根据交易风格设置盈利限制
							if(tradeStyle == TradeStyle.CONSERVATIVE) {
								takeProfitCode = fibInfo.getTakeProfit_v3(code, priceInfo.getPriceDoubleValue(), u.getProfit(), u.getProfitLimit());
							}
							
							logger.debug("当前交易风格：{},所处点位：{}，止盈点位：{}", tradeStyle.getMemo(), code.getDescription(), takeProfitCode.getDescription());
							
							//回踩单判断
							TradeStepBackStatus tradeStepBackStatus = TradeStepBackStatus.valueOf(u.getTradeStepBack());
							if(code.gt(FibCode.FIB1) && tradeStepBackStatus == TradeStepBackStatus.CLOSE) {
								continue;
							}
							
							//追踪委托价
							activationPriceValue = new BigDecimal(
									PriceUtil.formatDoubleDecimal(
											PriceUtil.calculateLongActivationPrice_v2(priceInfo.getPriceDoubleValue(), activationPriceRatio, fibInfo.getFibValue(takeProfitCode)),
											decimalNum
									)
								);

							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(takeProfitCode),decimalNum)
											);
							
						} else if(autoTradeType == AutoTradeType.AREA_INDEX) {
							double profitPrice = openPrice.getSecondTakeProfit();
							if(tradeStyle == TradeStyle.CONSERVATIVE) {
								profitPrice = openPrice.getAreaTakeProfit(priceInfo.getPriceDoubleValue(), openPrice, u.getMonitorProfit(), u.getProfitLimit(), QuotationMode.LONG);
							}
							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(profitPrice,decimalNum)
											);
						} else if(autoTradeType == AutoTradeType.EMA_INDEX) {
							//指数均线不设置止盈 由追踪委托来自动平仓
							//profitOrderEnabled = ProfitOrderEnabled.CLOSE;
							//强制启用追踪委托
							//callbackRateEnabled = CallbackRateEnabled.OPEN;
						} else if(autoTradeType == AutoTradeType.PRICE_ACTION) {
							FibCode code = openPrice.getCode();
							takeProfitCode = fibInfo.getPriceActionTakeProfit_v1(code);
							if(tradeStyle == TradeStyle.CONSERVATIVE) {
								takeProfitCode = fibInfo.getPriceActionTakeProfit(code, priceInfo.getPriceDoubleValue(), u.getProfit(), u.getProfitLimit());
							}
							//突破交易控制
							if(breakthroughTradeStatus == BreakthroughTradeStatus.CLOSE && code.gte(FibCode.FIB1)) {
								continue;
							}
							
							//追踪委托价
							activationPriceValue = new BigDecimal(
									PriceUtil.formatDoubleDecimal(
											PriceUtil.calculateLongActivationPrice_v2(priceInfo.getPriceDoubleValue(), activationPriceRatio, fibInfo.getFibValue(takeProfitCode)),
											decimalNum
									)
								);
							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(takeProfitCode),decimalNum)
											);
						}
						
						stopLoss = new BigDecimal(
								PriceUtil.formatDoubleDecimal(PriceUtil.rectificationCutLossLongPrice_v3(Double.valueOf(priceInfo.getPrice()), u.getCutLoss()),decimalNum));
						
						//计算预计盈利百分比
						profitPercent = PriceUtil.getRiseFluctuationPercentage(Double.valueOf(priceInfo.getPrice()),takeProfit.doubleValue());
						
						//盈利太少则不做交易
						if((profitPercent * 100) < u.getProfit()) {
							logger.debug(pair + "预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%，不做交易");
							continue;
						}
						
						//根据交易风格设置盈利限制
						if(tradeStyle == TradeStyle.CONSERVATIVE && autoTradeType == AutoTradeType.DEFAULT) {
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
					
					if(profitOrderEnabled == ProfitOrderEnabled.OPEN) {
						//多头的止盈价格必须大于当前价格
						if(!(priceInfo.getPriceDoubleValue() < takeProfit.doubleValue() && priceInfo.getPriceDoubleValue() > stopLoss.doubleValue())) {
							continue;
						}
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
					
					List<LeverageBracketInfo> list = binanceRestTradeService.getLeverageBracketInfo(binanceApiKey, binanceSecretKey, pair);
					
					int maxLeverage = LeverageBracketUtil.getMaxLeverageBracketInfo(list).getInitialLeverage();
					int updateLeverage = u.getLeverage() > maxLeverage ? maxLeverage : u.getLeverage();
					
					int leverage = sc.getLeverage();
					
					logger.debug(pair + "当前杠杆倍数：" + leverage + "倍");
					if(leverage != updateLeverage) {
						logger.debug("开始修改" + pair + "杠杆倍数");
						binanceRestTradeService.leverage(binanceApiKey, binanceSecretKey, pair, updateLeverage);
					}
					
					//持仓价值 = 持仓数量 * 价格
					double order_value = quantity.doubleValue() * priceInfo.getPriceDoubleValue();
					
					if(order_value > u.getPositionValue()) {
						logger.debug(pair + "下单数量仓位价值超过" + u.getPositionValue() + marginAsset);
						continue;
					}
					
					//获取用户手续费率
					CommissionRate rate = binanceRestTradeService.getCommissionRate(binanceApiKey, binanceSecretKey, pair);
					double orderBalance = (order_value / updateLeverage);//持仓所需保证金
					
					//开仓所需金额 = 持仓所需保证金 + (持仓所需保证金 x 用户手续费率【吃单费率】 x 2)
					double minOrder_value = orderBalance + (orderBalance * rate.getTakerCommissionRateDoubleValue() * 2);
					
					String availableBalanceStr = binanceWebsocketTradeService.availableBalance(binanceApiKey, binanceSecretKey, marginAsset);
					if(Double.valueOf(availableBalanceStr) < minOrder_value) {
						logger.debug("用户" + u.getUsername() + "可下单金额小于" + minOrder_value + marginAsset);
						continue;
					}
					
					//仓位数量限制
					int positionCount = binanceRestTradeService.countPosition(binanceApiKey, binanceSecretKey);
					if(positionCount >= u.getPositionCountLimit()) {
						logger.debug("用户" + u.getUsername() + "当前仓位数量不能超过" + u.getPositionCountLimit());
						continue;
					}
					
					List<PositionInfo> positionList = binanceRestTradeService.getPositionInfo(binanceApiKey, binanceSecretKey, pair, ps);
					if(!CollectionUtils.isEmpty(positionList)) {
						logger.debug("用户" + u.getUsername() + "在" + pair + "交易对中已有持仓");
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
							sendEmail(u, "修改持仓模式失败 " + dateStr, "修改持仓模式失败，失败原因：" + result.getMsg(), tradeUserEmail);
						}
					}
					
					MarginType marginType = MarginType.resolve(sc.getMarginType());
					
					logger.debug(pair + "当前保证金模式：" + marginType);
					
					if(marginType != MarginType.ISOLATED) {
						logger.debug("修改" + pair + "保证金模式为：" + MarginType.ISOLATED);
						binanceRestTradeService.marginType(binanceApiKey, binanceSecretKey, pair, MarginType.ISOLATED);
					}
					
					if(openPrice != null && openPrice.getStopLossLimit() > 0) {
						priceInfo = binanceWebsocketTradeService.getPrice(pair);
						double stopLossLimit = openPrice.getStopLossLimit();
						if(priceInfo.getPriceDoubleValue() <= stopLossLimit) {
							continue;
						}
						if(stopLoss.doubleValue() < stopLossLimit) {
							stopLoss = new BigDecimal(PriceUtil.formatDoubleDecimal(stopLossLimit, decimalNum));
						}
					}
					
					binanceWebsocketTradeService.tradeMarket(binanceApiKey, binanceSecretKey, pair, PositionSide.LONG, quantity, stopLoss, takeProfit, 
							callbackRateEnabled, activationPriceValue, callbackRateValue, profitOrderEnabled);

					//开仓邮件通知
					String subject_ = "";
					String pnlStr = PriceUtil.formatDoubleDecimal(profitPercent * 100, 2);
					
					if((autoTradeType == AutoTradeType.FIB_RET
							|| autoTradeType == AutoTradeType.PRICE_ACTION ) && fibInfo != null) {
						
						subject_ = String.format("%s多头仓位已下单[%s][%s(%s)][PNL:%s%%] %s", 
								pair, 
								fibInfo.getLevel().getLabel(),
								openPrice.getCode().getDescription(), 
								PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(openPrice.getCode()),fibInfo.getDecimalPoint()),
								pnlStr, 
								dateStr);
						
					} else {
						subject_ = String.format("%s多头仓位已下单[PNL:%s%%] %s", pair, pnlStr, dateStr);
					}
					
					String text_ = StringUtil.formatLongMessage_v2(pair, Double.valueOf(priceInfo.getPrice()), stopLoss.doubleValue(), 
							takeProfit.doubleValue(), decimalNum, pnlStr);
					
					if(fibInfo != null) {
						text_ += "\n\n" + fibInfo.toString();
					}
					
					if(recvTradeStatus == RecvTradeStatus.OPEN) {
						sendEmail(u, subject_, text_, tradeUserEmail);
					}
					
				} catch (Exception e) {
					String title = "下单" + pair + "多头仓位时出现异常";
					String message = e.getMessage();
					if(e instanceof OrderPlaceException) {
						title = ((OrderPlaceException)e).getTitle();
					}
					sendEmail(u, title + " " + dateStr, message, tradeUserEmail);
					logger.error(e.getMessage(), e);
				}
					
			} else {//做空
				
				try {

					PriceInfo priceInfo = binanceWebsocketTradeService.getPrice(pair);
					
					//计算追踪止损触发价
					BigDecimal activationPriceValue = new BigDecimal(
							PriceUtil.formatDoubleDecimal(PriceUtil.calculateShortActivationPrice(priceInfo.getPriceDoubleValue(), activationPriceRatio), decimalNum)
							);
					
					if(autoTradeType == AutoTradeType.DEFAULT) {//自定义止盈止损
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
							
							FibCode code = openPrice.getCode();
							
							if(code.lt(u.getFibLevelType().getLevelCode())) {
								continue;
							}

							takeProfitCode = fibInfo.getTakeProfit_v2(code);
							
							//根据交易风格设置盈利限制
							if(tradeStyle == TradeStyle.CONSERVATIVE) {
								takeProfitCode = fibInfo.getTakeProfit_v3(code, priceInfo.getPriceDoubleValue(), u.getProfit(), u.getProfitLimit());
							}
							
							logger.debug("当前交易风格：{},所处点位：{}，止盈点位：{}", tradeStyle.getMemo(), code.getDescription(), takeProfitCode.getDescription());
							
							//回踩单判断
							TradeStepBackStatus tradeStepBackStatus = TradeStepBackStatus.valueOf(u.getTradeStepBack());
							if(code.gt(FibCode.FIB1) && tradeStepBackStatus == TradeStepBackStatus.CLOSE) {
								continue;
							}
							
							//追踪委托价
							activationPriceValue = new BigDecimal(
									PriceUtil.formatDoubleDecimal(
											PriceUtil.calculateShortActivationPrice_v2(priceInfo.getPriceDoubleValue(), activationPriceRatio, fibInfo.getFibValue(takeProfitCode)),
											decimalNum
									)
								);
							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(takeProfitCode),decimalNum)
											);
							
						} else if(autoTradeType == AutoTradeType.AREA_INDEX) {
							double profitPrice = openPrice.getSecondTakeProfit();
							if(tradeStyle == TradeStyle.CONSERVATIVE) {
								profitPrice = openPrice.getAreaTakeProfit(priceInfo.getPriceDoubleValue(), openPrice, u.getMonitorProfit(), u.getProfitLimit(), QuotationMode.SHORT);
							}
							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(profitPrice,decimalNum)
											);
						} else if(autoTradeType == AutoTradeType.EMA_INDEX) {
							//指数均线不设置止盈 由追踪委托来自动平仓
							//profitOrderEnabled = ProfitOrderEnabled.CLOSE;
							//强制启用追踪委托
							//callbackRateEnabled = CallbackRateEnabled.OPEN;
							
						} else if(autoTradeType == AutoTradeType.PRICE_ACTION) {
							FibCode code = openPrice.getCode();
							takeProfitCode = fibInfo.getPriceActionTakeProfit_v1(code);
							if(tradeStyle == TradeStyle.CONSERVATIVE) {
								takeProfitCode = fibInfo.getPriceActionTakeProfit(code, priceInfo.getPriceDoubleValue(), u.getProfit(), u.getProfitLimit());
							}
							//突破交易控制
							if(breakthroughTradeStatus == BreakthroughTradeStatus.CLOSE && code.gte(FibCode.FIB1)) {
								continue;
							}
							
							//追踪委托价
							/*FibCode next = fibInfo.getPriceActionActivationPriceCode(code);
							activationPriceValue = new BigDecimal(
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(next), decimalNum)
									);*/
							activationPriceValue = new BigDecimal(
									PriceUtil.formatDoubleDecimal(
											PriceUtil.calculateShortActivationPrice_v2(priceInfo.getPriceDoubleValue(), activationPriceRatio, fibInfo.getFibValue(takeProfitCode)),
											decimalNum
									)
								);
							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(takeProfitCode),decimalNum)
											);
						}
						
						stopLoss = new BigDecimal(
								PriceUtil.formatDoubleDecimal(PriceUtil.rectificationCutLossShortPrice_v3(Double.valueOf(priceInfo.getPrice()), u.getCutLoss()), decimalNum)
										);
						
						//计算预计盈利百分比
						profitPercent = PriceUtil.getFallFluctuationPercentage(Double.valueOf(priceInfo.getPrice()),takeProfit.doubleValue());
						
						//盈利太少则不做交易
						if((profitPercent * 100) < u.getProfit()) {
							logger.debug(pair + "预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%，不做交易");
							continue;
						}
						
						//根据交易风格设置盈利限制
						if(tradeStyle == TradeStyle.CONSERVATIVE && autoTradeType == AutoTradeType.DEFAULT) {
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
					
					if(profitOrderEnabled == ProfitOrderEnabled.OPEN) {
						//空头止盈价格必须小于当前价格
						if(!(priceInfo.getPriceDoubleValue() > takeProfit.doubleValue() && priceInfo.getPriceDoubleValue() < stopLoss.doubleValue())) {
							continue;
						}
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

					List<LeverageBracketInfo> list = binanceRestTradeService.getLeverageBracketInfo(binanceApiKey, binanceSecretKey, pair);
					
					int maxLeverage = LeverageBracketUtil.getMaxLeverageBracketInfo(list).getInitialLeverage();
					int updateLeverage = u.getLeverage() > maxLeverage ? maxLeverage : u.getLeverage();
					
					int leverage = sc.getLeverage();
					
					logger.debug(pair + "当前杠杆倍数：" + leverage + "倍");
					
					if(leverage != updateLeverage) {
						logger.debug("开始修改" + pair + "杠杆倍数");
						binanceRestTradeService.leverage(binanceApiKey, binanceSecretKey, pair, updateLeverage);
					}
					
					//持仓价值 = 持仓数量 * 价格
					double order_value = quantity.doubleValue() * priceInfo.getPriceDoubleValue();
					
					if(order_value > u.getPositionValue()) {
						logger.debug(pair + "下单数量仓位价值超过" + u.getPositionValue() + marginAsset);
						continue;
					}
					
					//获取用户手续费率
					CommissionRate rate = binanceRestTradeService.getCommissionRate(binanceApiKey, binanceSecretKey, pair);
					double orderBalance = (order_value / updateLeverage);//持仓所需保证金
					
					//开仓所需金额 = 持仓所需保证金 + (持仓所需保证金 x 用户手续费率【吃单费率】 x 2)
					double minOrder_value = orderBalance + (orderBalance * rate.getTakerCommissionRateDoubleValue() * 2);
					
					String availableBalanceStr = binanceWebsocketTradeService.availableBalance(binanceApiKey, binanceSecretKey, marginAsset);
					if(Double.valueOf(availableBalanceStr) < minOrder_value) {
						logger.debug("用户" + u.getUsername() + "可下单金额小于" + minOrder_value + marginAsset);
						continue;
					}
					
					//仓位数量限制
					int positionCount = binanceRestTradeService.countPosition(binanceApiKey, binanceSecretKey);
					if(positionCount >= u.getPositionCountLimit()) {
						logger.debug("用户" + u.getUsername() + "当前仓位数量不能超过" + u.getPositionCountLimit());
						continue;
					}

					List<PositionInfo> positionList = binanceRestTradeService.getPositionInfo(binanceApiKey, binanceSecretKey, pair, ps);
					if(!CollectionUtils.isEmpty(positionList)) {
						logger.debug("用户" + u.getUsername() + "在" + pair + "交易对中已有持仓");
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
							sendEmail(u, "修改持仓模式失败 " + dateStr, "修改持仓模式失败，失败原因：" + result.getMsg(), tradeUserEmail);
						}
					}
					
					MarginType marginType = MarginType.resolve(sc.getMarginType());

					logger.debug(pair + "当前保证金模式：" + marginType);
					
					if(marginType != MarginType.ISOLATED) {
						logger.debug("修改" + pair + "保证金模式为：" + MarginType.ISOLATED);
						binanceRestTradeService.marginType(binanceApiKey, binanceSecretKey, pair, MarginType.ISOLATED);
					}
					
					if(openPrice != null && openPrice.getStopLossLimit() > 0) {
						priceInfo = binanceWebsocketTradeService.getPrice(pair);
						double stopLossLimit = openPrice.getStopLossLimit();
						if(priceInfo.getPriceDoubleValue() >= stopLossLimit) {
							continue;
						}
						if(stopLoss.doubleValue() > stopLossLimit) {
							stopLoss = new BigDecimal(PriceUtil.formatDoubleDecimal(stopLossLimit, decimalNum));
						}
					}
					
					binanceWebsocketTradeService.tradeMarket(binanceApiKey, binanceSecretKey, pair, PositionSide.SHORT, quantity, stopLoss, takeProfit, 
							callbackRateEnabled, activationPriceValue, callbackRateValue, profitOrderEnabled);
					
					//开仓邮件通知
					String subject_ = "";
					String pnlStr = PriceUtil.formatDoubleDecimal(profitPercent * 100, 2);
					
					if((autoTradeType == AutoTradeType.FIB_RET
							|| autoTradeType == AutoTradeType.PRICE_ACTION) && fibInfo != null) {
						
						subject_ = String.format("%s空头仓位已下单[%s][%s(%s)][PNL:%s%%] %s", 
								pair, 
								fibInfo.getLevel().getLabel(),
								openPrice.getCode().getDescription(), 
								PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(openPrice.getCode()),fibInfo.getDecimalPoint()),
								pnlStr, 
								dateStr);
						
					} else {
						subject_ = String.format("%s空头仓位已下单[PNL:%s%%] %s", pair, pnlStr, dateStr);
					}
					
					String text_ = StringUtil.formatShortMessage_v2(pair, Double.valueOf(priceInfo.getPrice()), takeProfit.doubleValue(), 
							stopLoss.doubleValue(), decimalNum, pnlStr);
					
					if(fibInfo != null) {
						text_ += "\n\n" + fibInfo.toString();
					}
					
					if(recvTradeStatus == RecvTradeStatus.OPEN) {
						sendEmail(u, subject_, text_, tradeUserEmail);
					}
					
				} catch (Exception e) {
					String title = "下单" + pair + "空头仓位时出现异常";
					String message = e.getMessage();
					if(e instanceof OrderPlaceException) {
						title = ((OrderPlaceException)e).getTitle();
					}
					sendEmail(u, title + " " + dateStr, message, tradeUserEmail);
					logger.error(e.getMessage(), e);
				}
				
			}
		}
		
	}
	
	public void sendEmail(User user, String subject,String text,String recEmail) {
		
	 	if(StringUtil.isNotEmpty(recEmail) && StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
			emailWorkTaskPool.add(new SendMailTask(user, subject, text, recEmail, userRepository));
	 	}
		
	}
	
	@Override
	public void futuresFibMonitor(List<Klines> list_1d, List<Klines> list_4h, List<Klines> list_1h,  List<Klines> list_15m) {
		
		FibInfoFactory factory = new FibInfoFactoryImpl_v2(list_1h, list_1h, list_15m);
		
		FibInfo fibInfo = factory.getFibInfo();
		
		if(!(factory.isLong() || factory.isShort())) {
			return;
		}
		
		List<Klines> fibAfterKlines = factory.getFibAfterKlines();
		
		if(factory.isLong()) {
			Klines afterLowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);
			openLong_v2(factory.getOpenPrices(), fibInfo, afterLowKlines, list_15m);
		} else if(factory.isShort()) {
			Klines afterHighKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);
			openShort_v2(factory.getOpenPrices(), fibInfo, afterHighKlines, list_15m);
		}
	}
	
	@Override
	public void futuresPriceAction(List<Klines> list, List<Klines> list_15m) {
		
		PriceActionFactory factory = new PriceActionFactoryImpl(list, list_15m);
		
		FibInfo fibInfo = factory.getFibInfo();
		
		List<Klines> fibAfterKlines = factory.getFibAfterKlines();
		
		if(factory.isLong()) {
			Klines afterLowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);
			openLong_priceAction(factory.getOpenPrices(), fibInfo, afterLowKlines, list_15m);
		} else if(factory.isShort()) {
			Klines afterHighKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);
			openShort_priceAction(factory.getOpenPrices(), fibInfo, afterHighKlines, list_15m);
		}
	}
	
	@Override
	public void eoptionMonitor(List<Klines> list, List<Klines> list_15m) {
		
		if(CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		Klines last = PriceUtil.getLastKlines(list_15m);
		String pair = last.getPair();
		//限制非期权交易对
		if(AppConfig.EOPTION_EXCHANGE_INFO.get(pair) == null) {
			return;
		}
		
		logger.debug("execute {} eoptionMonitor." , pair);
		
		PriceActionFactory factory = new PriceActionFactoryImpl(list, list_15m);
		
		FibInfo fibInfo = factory.getFibInfo();
		
		List<Klines> fibAfterKlines = factory.getFibAfterKlines();
		
		if(factory.isLong()) {
			Klines afterLowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);
			openLong_eOption(factory.getOpenPrices(), fibInfo, afterLowKlines, list_15m);
		} else if(factory.isShort()) {
			Klines afterHighKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);
			openShort_eOption(factory.getOpenPrices(), fibInfo, afterHighKlines, list_15m);
		}
		
	}
	
	@Override
	public void consolidationAreaMonitor(List<Klines> list_1d, List<Klines> list, List<Klines> list_15m) {
		AreaFactory factory = new AreaFactoryImpl(list_1d, list, list_15m);
		
		if(!(factory.isLong() || factory.isShort())) {
			return;
		}
		
		Klines last = PriceUtil.getLastKlines(list_15m);
		String pair = last.getPair();
		
		double currentPrice = last.getClosePriceDoubleValue();
		
		OpenInterestHist oih = openInterestHistRepository.findOneBySymbol(pair);
		
		List<Klines> fibAfterKlines = factory.getFibAfterKlines();
		Klines afterLowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);
		Klines afterHighKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);
		List<OpenPrice> openPrices = factory.getOpenPrices();
		
		for (int index = 0; index < openPrices.size(); index++) {
			OpenPrice price = openPrices.get(index);
			if(factory.isLong() && PriceUtil.isBreachLong(last, price.getPrice()) 
					&& !PriceUtil.isObsoleteLong(afterLowKlines, openPrices, index)
					&& !PriceUtil.isTraded(price, factory)) {
				
				this.tradingTaskPool.add(new TradingTask(this, pair, PositionSide.LONG, 0, 0, price, null, AutoTradeType.AREA_INDEX, last.getDecimalNum()));
				
				List<User> userList = userRepository.queryAllUserByAreaMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					if(!PairPolicyUtil.verifyPairPolicy(u.getPairPolicySelected(), pair, u.getMonitorPolicyType())) {
						continue;
					}
					
					if(oih.getTradeNumber() < u.getTradeNumberMonitor()) {
						continue;
					}
					
					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					double profitPrice = price.getSecondTakeProfit();
					//保守的交易风格
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						profitPrice = price.getAreaTakeProfit(currentPrice, price, u.getMonitorProfit(), u.getProfitLimit(), QuotationMode.LONG);
					}
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getRiseFluctuationPercentage(currentPrice, profitPrice) * 100;
					
					if(profitPercent < u.getMonitorProfit()) {
						continue;
					}
					
					//开仓订阅提醒
					String subject = String.format("%s永续合约做多机会(PNL:%s%%) %s", pair,
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatLongMessage(pair, currentPrice, PriceUtil.rectificationCutLossLongPrice_v3(currentPrice, u.getCutLoss()), 
							profitPrice, last.getDecimalNum());
					
					sendEmail(u, subject, text, u.getUsername());
					
				}
				
				
			} else if(factory.isShort() && PriceUtil.isBreachShort(last, price.getPrice()) 
					&& !PriceUtil.isObsoleteShort(afterHighKlines, openPrices, index)
					&& !PriceUtil.isTraded(price, factory)) {
				
				this.tradingTaskPool.add(new TradingTask(this, pair, PositionSide.SHORT, 0, 0, price, null, AutoTradeType.AREA_INDEX, last.getDecimalNum()));
				
				List<User> userList = userRepository.queryAllUserByAreaMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					if(!PairPolicyUtil.verifyPairPolicy(u.getPairPolicySelected(), pair, u.getMonitorPolicyType())) {
						continue;
					}
					
					if(oih.getTradeNumber() < u.getTradeNumberMonitor()) {
						continue;
					}
					
					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					double profitPrice = price.getSecondTakeProfit();
					//保守的交易风格
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						profitPrice = price.getAreaTakeProfit(currentPrice, price, u.getMonitorProfit(), u.getProfitLimit(), QuotationMode.SHORT);
					}
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getFallFluctuationPercentage(currentPrice, profitPrice) * 100;
					
					if(profitPercent < u.getMonitorProfit()) {
						continue;
					}
					
					//开仓订阅提醒
					String subject = String.format("%s永续合约做空机会(PNL:%s%%) %s", pair,
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatLongMessage(pair, currentPrice, PriceUtil.rectificationCutLossShortPrice_v3(currentPrice, u.getCutLoss()), 
							profitPrice, last.getDecimalNum());
					
					sendEmail(u, subject, text, u.getUsername());
				}
			}
		}
	}
	
	@Override
	public void openLong_priceAction(List<OpenPrice> openPrices, FibInfo fibInfo, Klines afterLowKlines,
			List<Klines> klinesList_hit) {
		if(fibInfo == null) {
			return;
		}
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		String pair = hitKline.getPair();
		
		OpenInterestHist oih = openInterestHistRepository.findOneBySymbol(pair);
		
		//FibCode[] codes = FibCode.values();
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePriceDoubleValue();
		//double openPrice = hitKline.getOpenPrice();
		//double lowPrice = hitKline.getLowPriceDoubleValue();
		//double hightPrice = hitKline.getHighPrice();
		double currentPrice = closePrice;
		
		for(int index = 0;index < openPrices.size(); index++) {
		
			OpenPrice openPrice = openPrices.get(index);
			double price = openPrice.getPrice();
			
			FibCode code = openPrice.getCode();//当前斐波那契点位
			
			if(//PriceUtil.isLong_v3(price, klinesList_hit)
					PriceUtil.isBreachLong(hitKline, price)
					&& !PriceUtil.isObsoleteLong(afterLowKlines, openPrices, index)
					&& !PriceUtil.isTradedPriceAction(price, fibInfo)
					) {
			
				//市价做多
				this.tradingTaskPool.add(new TradingTask(this, pair, PositionSide.LONG, 0, 0, openPrice, fibInfo, AutoTradeType.PRICE_ACTION, fibInfo.getDecimalPoint()));
				
				//
				List<User> userList = userRepository.queryAllUserByEmaMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					if(!PairPolicyUtil.verifyPairPolicy(u.getPairPolicySelected(), pair, u.getMonitorPolicyType())) {
						continue;
					}
					
					if(oih.getTradeNumber() < u.getTradeNumberMonitor()) {
						continue;
					}
					
					//是否监控突破交易
					BreakthroughTradeStatus breakthroughTradeStatus = BreakthroughTradeStatus.valueOf(u.getBreakthroughMonitor());
					if(breakthroughTradeStatus == BreakthroughTradeStatus.CLOSE && code.gte(FibCode.FIB1)) {
						continue;
					}
					
					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					FibCode closePpositionCode = fibInfo.getPriceActionTakeProfit_v1(code);//止盈点位
					
					//保守的交易风格
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						closePpositionCode = fibInfo.getPriceActionTakeProfit(code, currentPrice, u.getMonitorProfit(), u.getProfitLimit());
					}
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getRiseFluctuationPercentage(currentPrice, fibInfo.getFibValue(closePpositionCode)) * 100;
					
					if(profitPercent < u.getMonitorProfit()) {
						continue;
					}
					
					//止盈价
					double profitPrice = fibInfo.getFibValue(closePpositionCode);

					//开仓订阅提醒
					String subject = String.format("%s永续合约%s(%s)[%s]强势价格行为(PNL:%s%%) %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatLongMessage(pair, currentPrice, PriceUtil.rectificationCutLossLongPrice_v3(currentPrice, u.getCutLoss()), 
							profitPrice, fibInfo.getDecimalPoint());
					
					text += "\r\n\r\n" + fibInfo.toString();
					
					sendEmail(u, subject, text, u.getUsername());
				}
				break;
			}
		}
	}

	@Override
	public void openShort_priceAction(List<OpenPrice> openPrices, FibInfo fibInfo, Klines afterHighKlines,
			List<Klines> klinesList_hit) {
		if(fibInfo == null) {
			return;
		}
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		
		//开盘、收盘、最低、最高价格
		double closePrice = hitKline.getClosePriceDoubleValue();
		//double openPrice = hitKline.getOpenPrice();
		//double lowPrice = hitKline.getLowPrice();
		//double hightPrice = hitKline.getHighPriceDoubleValue();
		double currentPrice = closePrice;
		String pair = hitKline.getPair();
		
		OpenInterestHist oih = openInterestHistRepository.findOneBySymbol(pair);
		
		//FibCode[] codes = FibCode.values();
		
		for(int index = 0;index < openPrices.size(); index++) {
			
			OpenPrice openPrice = openPrices.get(index);
			double price = openPrice.getPrice();
			
			FibCode code = openPrice.getCode();//当前斐波那契点位
			
			if(//PriceUtil.isShort_v3(price, klinesList_hit)
					PriceUtil.isBreachShort(hitKline, price)
					&& !PriceUtil.isObsoleteShort(afterHighKlines, openPrices, index)
					&& !PriceUtil.isTradedPriceAction(price, fibInfo)
					) {
			
				//市价做空
				this.tradingTaskPool.add(new TradingTask(this, pair, PositionSide.SHORT, 0, 0, openPrice,  fibInfo, AutoTradeType.PRICE_ACTION, fibInfo.getDecimalPoint()));

				//
				List<User> userList = userRepository.queryAllUserByEmaMonitor(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					if(!PairPolicyUtil.verifyPairPolicy(u.getPairPolicySelected(), pair, u.getMonitorPolicyType())) {
						continue;
					}
					
					if(oih.getTradeNumber() < u.getTradeNumberMonitor()) {
						continue;
					}
					
					//是否监控突破交易
					BreakthroughTradeStatus breakthroughTradeStatus = BreakthroughTradeStatus.valueOf(u.getBreakthroughMonitor());
					if(breakthroughTradeStatus == BreakthroughTradeStatus.CLOSE && code.gte(FibCode.FIB1)) {
						continue;
					}
					
					//根据交易风格设置盈利限制
					TradeStyle tradeStyle = TradeStyle.valueOf(u.getTradeStyle());
					
					FibCode closePpositionCode = fibInfo.getPriceActionTakeProfit_v1(code);//止盈点位
					
					//保守的交易风格
					if(tradeStyle == TradeStyle.CONSERVATIVE) {
						closePpositionCode = fibInfo.getPriceActionTakeProfit(code, currentPrice, u.getMonitorProfit(), u.getProfitLimit());
					}
					
					//计算预计盈利百分比
					double profitPercent = PriceUtil.getFallFluctuationPercentage(currentPrice, fibInfo.getFibValue(closePpositionCode)) * 100;
					
					if(profitPercent < u.getMonitorProfit()) {
						continue;
					}
					
					//止盈价
					double profitPrice = fibInfo.getFibValue(closePpositionCode);
					
					String subject = String.format("%s永续合约%s(%s)[%s]颓势价格行为(PNL:%s%%) %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							PriceUtil.formatDoubleDecimal(profitPercent, 2),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatShortMessage(pair, currentPrice, profitPrice, 
							PriceUtil.rectificationCutLossShortPrice_v3(currentPrice, u.getCutLoss()), fibInfo.getDecimalPoint());
					
					text += "\r\n\r\n" + fibInfo.toString();
					
					
					sendEmail(u, subject,text, u.getUsername());
				}
				break;
			}
		}
	}
	
	@Override
	public void openLong_eOption(List<OpenPrice> openPrices, FibInfo fibInfo, Klines afterLowKlines,
			List<Klines> klinesList_hit) {
		
		if(fibInfo == null) {
			return;
		}
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		String pair = hitKline.getPair();
		
		for(int index = 0;index < openPrices.size(); index++) {
		
			OpenPrice openPrice = openPrices.get(index);
			double price = openPrice.getPrice();
			
			if(PriceUtil.isBreachLong(hitKline, price)
					&& !PriceUtil.isObsoleteLong(afterLowKlines, openPrices, index)
					) {
			
				//
				List<User> userList = userRepository.queryAllUserByEoptionsStatus(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					if(!PairPolicyUtil.verifyPairPolicy(u.getPairPolicySelected(), pair, u.getMonitorPolicyType())) {
						continue;
					}

					//开仓订阅提醒
					String subject = String.format("%s看涨期权(%s)买入机会 %s", 
							pair, 
							PriceUtil.formatDoubleDecimal(price, fibInfo.getDecimalPoint()),
							DateFormatUtil.format(new Date()));
					
					String text = fibInfo.toString();
					
					sendEmail(u, subject, text, u.getUsername());
				}
				break;
			}
		}
	}

	@Override
	public void openShort_eOption(List<OpenPrice> openPrices, FibInfo fibInfo, Klines afterHighKlines,
			List<Klines> klinesList_hit) {
		
		if(fibInfo == null) {
			return;
		}
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		
		String pair = hitKline.getPair();
		
		for(int index = 0;index < openPrices.size(); index++) {
			
			OpenPrice openPrice = openPrices.get(index);
			double price = openPrice.getPrice();
			
			if(PriceUtil.isBreachShort(hitKline, price)
					&& !PriceUtil.isObsoleteShort(afterHighKlines, openPrices, index)
					) {
				
				//
				List<User> userList = userRepository.queryAllUserByEoptionsStatus(MonitorStatus.OPEN);
				
				for(User u : userList) {
					
					if(!PairPolicyUtil.verifyPairPolicy(u.getPairPolicySelected(), pair, u.getMonitorPolicyType())) {
						continue;
					}
					
					//开仓订阅提醒
					String subject = String.format("%s看跌期权(%s)买入机会 %s", 
							pair, 
							PriceUtil.formatDoubleDecimal(price, fibInfo.getDecimalPoint()),
							DateFormatUtil.format(new Date()));
					
					String text = fibInfo.toString();
					
					sendEmail(u, subject,text, u.getUsername());
				}
				break;
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
			
			if(PriceUtil.hitPrice(klines, price)) {
				
				User user = userRepository.queryByUsername(info.getOwner());
				
				emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));
				
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
						
						this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.SHORT, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					}
					
					if(type == LongOrShortType.LONG) { // 做多
						
						FibInfo fibInfo = new FibInfo(highValue, lowValue, klines.getDecimalNum(), FibLevel.LEVEL_1);
						
						this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.LONG, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					}
				}
			}
			
		}
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
			
			if(PriceUtil.hitPrice(klines, price0)) {
				upOrLowStr = price0 > price1 ? "上" : "下";
				String dateStr = DateFormatUtil.format(new Date());
				String subject = String.format("%s永续合约价格已到达盘整区%s边缘%s %s", klines.getPair(), upOrLowStr, PriceUtil.formatDoubleDecimal(price0,klines.getDecimalNum()),dateStr);
				String text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price0,klines.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(price1,klines.getDecimalNum()),
						klines.getClosePrice());

				User user = userRepository.queryByUsername(info.getOwner());
				
				emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));

				FibInfo fibInfo = new FibInfo(price1, price0, klines.getDecimalNum(), FibLevel.LEVEL_1);
				
				if(upOrLowStr.equals("上")) {//做空
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.SHORT, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
				} else {//做多
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.LONG, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
				}
			}
			
			if(PriceUtil.hitPrice(klines, price1)) {
				upOrLowStr = price1 > price0 ? "上" : "下";
				String dateStr = DateFormatUtil.format(new Date());
				String subject = String.format("%s永续合约价格已到达盘整区%s边缘%s %s", klines.getPair(), upOrLowStr, PriceUtil.formatDoubleDecimal(price1,klines.getDecimalNum()),dateStr);
				String text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price0,klines.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(price1,klines.getDecimalNum()),
						klines.getClosePrice());

				User user = userRepository.queryByUsername(info.getOwner());
				
				emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));
				
				FibInfo fibInfo = new FibInfo(price0, price1, klines.getDecimalNum(), FibLevel.LEVEL_1);
				if(upOrLowStr.equals("上")) {//做空
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.SHORT, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
				} else {//做多
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.LONG, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
				}
			}
			
		}
	}
	
	@Override
	public void fixedRangeValumeProfile(Klines klines,ShapeInfo info) {
		String dateStr = DateFormatUtil.format(new Date());
		User user = userRepository.queryByUsername(info.getOwner());
		if(user == null) {
			return;
		}
		//价格坐标
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		if(pointsJsonArray.length() > 1) {
			JSONObject points = pointsJsonArray.getJSONObject(0);
			//double price0 = points.getDouble("price");
			long time0 = points.getLong("time") * 1000;
			
			JSONObject points1 = pointsJsonArray.getJSONObject(1);
			//double price1 = points1.getDouble("price");
			long time1 = points1.getLong("time") * 1000;
			List<Klines> list = klinesRepository.findByTimeLimit(info.getSymbol(), info.getInervalType(), time0, time1);
			if(CollectionUtils.isEmpty(list)) {
				return;
			}
			Klines high = PriceUtil.getMaxPriceKLine(list);
			Klines low = PriceUtil.getMinPriceKLine(list);
			Klines highBody = PriceUtil.getMaxBodyHighPriceKLine(list);
			Klines lowBody = PriceUtil.getMinBodyHighPriceKLine(list);
			double highPrice = high.getHighPriceDoubleValue();
			double lowPrice = low.getLowPriceDoubleValue();
			double highBodyPrice = highBody.getBodyHighPriceDoubleValue();
			double lowBodyPrice = lowBody.getBodyLowPriceDoubleValue();
			
			logger.debug("{}永续合约盘整区最高价格：{}，最低价格：{}，顶部价格：{}，底部价格：{}", klines.getPair(), highPrice, lowPrice, highBodyPrice, lowBodyPrice);
			
			if(PriceUtil.hitPrice(klines, highPrice)) {
				String subject = String.format("%s永续合约价格已到达盘整区最高价%s %s", klines.getPair(), PriceUtil.formatDoubleDecimal(highPrice, klines.getDecimalNum()), dateStr);
				String text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(lowPrice,klines.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(highPrice,klines.getDecimalNum()),
						klines.getClosePrice());
				sendEmail(user, subject, text, user.getUsername());
			} else if(PriceUtil.hitPrice(klines, highBodyPrice)) {
				String subject = String.format("%s永续合约价格到达盘整区顶部 %s", klines.getPair(), dateStr);
				String text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(lowPrice,klines.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(highPrice,klines.getDecimalNum()),
						klines.getClosePrice());
				sendEmail(user, subject, text, user.getUsername());
			}
			
			if(PriceUtil.hitPrice(klines, lowPrice)) {
				String subject = String.format("%s永续合约价格已到达盘整区最低价%s %s", klines.getPair(), PriceUtil.formatDoubleDecimal(lowPrice, klines.getDecimalNum()), dateStr);
				String text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(lowPrice,klines.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(highPrice,klines.getDecimalNum()),
						klines.getClosePrice());
				sendEmail(user, subject, text, user.getUsername());
			} else if(PriceUtil.hitPrice(klines, lowBodyPrice)) {
				String subject = String.format("%s永续合约价格到达盘整区底部 %s", klines.getPair(), dateStr);
				String text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(lowPrice,klines.getDecimalNum()),
						PriceUtil.formatDoubleDecimal(highPrice,klines.getDecimalNum()),
						klines.getClosePrice());
				sendEmail(user, subject, text, user.getUsername());
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
			if(PriceUtil.hitPrice(klines, resultPrice)) {
				
				User user = userRepository.queryByUsername(info.getOwner());
				
				emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));
				
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
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.LONG, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
				} else {//做空
					List<Klines> fib_klines_list = PriceUtil.subList(low, list);
					Klines fibHigh = PriceUtil.getMaxPriceKLine(fib_klines_list);
					double lowValue = low.getLowPriceDoubleValue();
					double highValue = fibHigh.getHighPriceDoubleValue();
					FibInfo fibInfo = new FibInfo(lowValue, highValue, klines.getDecimalNum(), FibLevel.LEVEL_1);
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.SHORT, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
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
			if(PriceUtil.hitPrice(klines, line0Price)) {
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

				User user = userRepository.queryByUsername(info.getOwner());
				
				emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));
				
				FibInfo fibInfo = new FibInfo(line1Price, line0Price, klines.getDecimalNum(), FibLevel.LEVEL_1);
				
				if(upOrLowStr.equals("上")) { //做空
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.SHORT, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
				} else { //做多
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.LONG, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
				}
			}
			
			//第二条直线
			if(PriceUtil.hitPrice(klines, line1Price)) {
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
				
				User user = userRepository.queryByUsername(info.getOwner());
				
				emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));
				
				FibInfo fibInfo = new FibInfo(line0Price, line1Price, klines.getDecimalNum(), FibLevel.LEVEL_1);
				
				if(upOrLowStr.equals("上")) { //做空
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.SHORT, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
				} else { //做多
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.LONG, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, fibInfo.getDecimalPoint()));
					
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
			if(PriceUtil.hitPrice(klines, acPrice)) {
				upOrLowStr = acPrice > bdPrice ? "上" : "下";
				subject = String.format("%s永续合约价格到达%s%s边缘%s %s", 
						klines.getPair(), 
						parallelChannelOrTrianglePattern, 
						upOrLowStr,
						PriceUtil.formatDoubleDecimal(acPrice,klines.getDecimalNum()),
						dateStr);
				fibInfo = new FibInfo(bdPrice, acPrice, klines.getDecimalNum(), FibLevel.LEVEL_1);
			}
			
			if(PriceUtil.hitPrice(klines, bdPrice)) {
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

				User user = userRepository.queryByUsername(info.getOwner());
				
				this.emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));
				
				if(upOrLowStr.equals("上")) {//做空
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.SHORT, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, klines.getDecimalNum()));
					
				} else {
					
					this.tradingTaskPool.add(new TradingTask(this, info.getSymbol(), PositionSide.LONG, 0, 0, null, fibInfo, AutoTradeType.DEFAULT, klines.getDecimalNum()));
					
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
			
			if(PriceUtil.hitPrice(klines, price)) {
				String subject = String.format("%s永续合约(%s)做多交易计划 %s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price, klines.getDecimalNum()),
						dateStr);
				
				double stopLossDoubleValue = price - stopLevel;
				double takeProfitDoubleValue = price + profitLevel;
				
				String text = StringUtil.formatLongMessage(klines.getPair(), price, stopLossDoubleValue, takeProfitDoubleValue, klines.getDecimalNum());

				User user = userRepository.queryByUsername(info.getOwner());
				
				this.emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));
				
				this.tradingTaskPool.add(new TradingTask(this, pair, PositionSide.LONG, stopLossDoubleValue, takeProfitDoubleValue, null, null, AutoTradeType.DEFAULT, klines.getDecimalNum()));
				
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
			
			if(PriceUtil.hitPrice(klines, price)) {
				String subject = String.format("%s永续合约(%s)做空交易计划 %s", 
						klines.getPair(),
						PriceUtil.formatDoubleDecimal(price, klines.getDecimalNum()),
						dateStr);
				
				double stopLossDoubleValue = price + stopLevel;
				double takeProfitDoubleValue = price - profitLevel;
				
				String text = StringUtil.formatLongMessage(klines.getPair(), price, stopLossDoubleValue, takeProfitDoubleValue, klines.getDecimalNum());

				User user = userRepository.queryByUsername(info.getOwner());
				
				this.emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));
				
				this.tradingTaskPool.add(new TradingTask(this, pair, PositionSide.SHORT, stopLossDoubleValue, takeProfitDoubleValue, null, null, AutoTradeType.DEFAULT, klines.getDecimalNum()));
				
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
				if(visible && PriceUtil.hitPrice(klines, price)) {
					String subject = String.format("%s永续合约价格已到达%s(%s) %s",
							klines.getPair(),
							coeff,
							PriceUtil.formatDoubleDecimal(price, klines.getDecimalNum()),
							dateStr);
					String text = fib.toString();
					
					User user = userRepository.queryByUsername(info.getOwner());
					
					this.emailWorkTaskPool.add(new SendMailTask(user, subject, text, info.getOwner(), userRepository));
				}
			}
		}
	}
	
	@Override
    public boolean checkData(List<Klines> list, ContractType contractType) {
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
                    	List<Klines> data = continuousKlines(current.getPair(), startTime, endTime, current.getInervalType(), QUERY_SPLIT.NOT_ENDTIME, contractType);
                    	logger.info(current.getPair() + "交易对" + current.getInterval() + "级别k线信息数据有缺矢，已同步" + data.size() 
                    				+ "条数据，缺失时间段：" + DateFormatUtil.format(startTime) + " ~ " + DateFormatUtil.format(endTime));
                    	klinesRepository.insert(data);
                    	//创建同步标识
                    	FileUtil.createFile(fileName);
                	} else {
                		logger.debug("{}交易对{}级别{}~{}缺失部分已是最新数据", current.getPair(), current.getInterval(), 
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
                    	klinesRepository.remove(_id, StringUtil.formatCollectionName(current));
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
	public boolean verifyUpdateDayKlines(List<Klines> list, ContractType contractType) {
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
			List<Klines> list_day = this.continuousKlines(last.getPair(), startTime, endTime, Inerval.INERVAL_1D, QUERY_SPLIT.NOT_ENDTIME,contractType);
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
	public void volumeMonitor(List<Klines> list_1d, List<Klines> list_4h, List<Klines> list_1h, List<Klines> list_15m) {
		
		if(CollectionUtils.isEmpty(list_15m) || list_15m.size() < 99) {
			return;
		}
		//list_1h.sort(new KlinesComparator(SortType.ASC));
		list_15m.sort(new KlinesComparator(SortType.ASC));
		
		PriceUtil.calculateEMA_7_25_99(list_15m);
		PriceUtil.calculateMACD(list_15m);
		//PriceUtil.calculateEMA_7_25_99(list_1h);
		//PriceUtil.calculateMACD(list_1h);
		
		int size = list_15m.size();
		
		Klines current = list_15m.get(size - 1);
		Klines parent = list_15m.get(size - 2);
		
		Klines last = PriceUtil.getLastKlines(list_15m);
		String pair = last.getPair();
		
		OpenInterestHist oih = openInterestHistRepository.findOneBySymbol(pair);
		
		String subject = "";
		String text = last.toString();
		
		String dateStr = DateFormatUtil.format(new Date());
		
		if(last.getEma25() > last.getEma99() && PriceUtil.isLong_v3(last.getEma25(), list_15m)) {//买入信号
			
			subject = String.format("%s永续合约买入信号 %s", pair, dateStr);
			
		} else if(last.getEma25() < last.getEma99() && PriceUtil.isShort_v3(last.getEma25(), list_15m) && current.getMacd() < parent.getMacd()) {//卖出信号
			
			subject = String.format("%s永续合约卖出信号 %s", pair, dateStr);
			
		}
		
		/*
		if(PriceUtil.verifyDecliningPrice_v11(current, parent) && current.getEma25() < current.getEma99()) {
			subject = String.format("%s永续合约开始下跌 %s", pair, dateStr);
		} else if(PriceUtil.verifyPowerful_v11(current, parent) && current.getEma25() > current.getEma99()) {
			subject = String.format("%s永续合约开始上涨 %s", pair, dateStr);
		} else if(current.getDea() > 0 && parent.getEma7() <= parent.getEma99() && current.getEma7() > current.getEma99()) {
			subject = String.format("%s永续合约买入信号 %s", pair, dateStr);
		} else if(current.getDea() < 0 && parent.getEma7() >= parent.getEma99() && current.getEma7() < current.getEma99()) {
			subject = String.format("%s永续合约卖出信号 %s", pair, dateStr);
		}*/
		
		if(StringUtil.isNotEmpty(subject)) {
			List <User> userList = userRepository.queryByVolumeMonitorStatus(VolumeMonitorStatus.OPEN);
			for(User u : userList) {
				
				if(oih.getTradeNumber() < u.getTradeNumberMonitor()) {
					continue;
				}

				if(!PairPolicyUtil.verifyPairPolicy(u.getPairPolicySelected(), pair, u.getMonitorPolicyType())) {
					continue;
				}
				
				sendEmail(u, subject, text, u.getUsername());
			}
		}
	}
}