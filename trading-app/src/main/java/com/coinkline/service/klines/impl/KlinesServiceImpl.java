package com.coinkline.service.klines.impl;

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
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.coinkline.binance.trade.rest.BinanceRestTradeService;
import com.coinkline.binance.trade.websocket.BinanceWebsocketTradeService;
import com.coinkline.config.AppConfig;
import com.coinkline.module.EMAType;
import com.coinkline.module.FibCode;
import com.coinkline.module.FibInfo;
import com.coinkline.module.FibKlinesData;
import com.coinkline.module.FibLevel;
import com.coinkline.module.HighOrLowHitPrice;
import com.coinkline.module.Inerval;
import com.coinkline.module.Klines;
import com.coinkline.module.LongOrShortType;
import com.coinkline.module.PriceFibInfo;
import com.coinkline.module.QUERY_SPLIT;
import com.coinkline.module.QuotationMode;
import com.coinkline.module.ResultCode;
import com.coinkline.module.ShapeInfo;
import com.coinkline.module.SortType;
import com.coinkline.module.area.ConsolidationArea;
import com.coinkline.module.binance.AutoTrade;
import com.coinkline.module.binance.AutoTradeType;
import com.coinkline.module.binance.BinanceOrderInfo;
import com.coinkline.module.binance.DrawTrade;
import com.coinkline.module.binance.MarginType;
import com.coinkline.module.binance.PriceInfo;
import com.coinkline.module.binance.Result;
import com.coinkline.module.binance.SymbolConfig;
import com.coinkline.module.result.DeclineAndStrength;
import com.coinkline.module.trading.PositionSide;
import com.coinkline.module.trading.TradingOrder;
import com.coinkline.module.user.User;
import com.coinkline.repository.high_low_hitprice.HighOrLowHitPriceRepository;
import com.coinkline.repository.klines.KlinesRepository;
import com.coinkline.repository.shape.ShapeRepository;
import com.coinkline.repository.trading.OrderRepository;
import com.coinkline.service.klines.KlinesService;
import com.coinkline.service.user.UserService;
import com.coinkline.trading_app.pool.WorkTaskPool;
import com.coinkline.trading_app.task.email.SendMailTask;
import com.util.CommandUtil;
import com.util.ConsolidationAreaUtil;
import com.util.DateFormatUtil;
import com.util.KlinesComparator;
import com.util.KlinesUtil;
import com.util.PriceUtil;
import com.util.StraightLineUtil;
import com.util.StringUtil;

@Service("klinesService")
public class KlinesServiceImpl implements KlinesService {

	private final Logger logger = LogManager.getLogger(KlinesServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private HighOrLowHitPriceRepository highOrLowHitPriceRepository;
	
	@Autowired
	private UserService userDetailsService;
	
	@Autowired
	private WorkTaskPool emailWorkTaskPool;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private KlinesRepository klinesRepository;
	
	@Autowired
	private BinanceRestTradeService binanceRestTradeService;
	
	@Autowired
	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	@Autowired
	private ShapeRepository shapeRepository;
	
	@Override
	public List<Klines> continuousKlines(String pair, long startTime, long endTime,
			String interval,QUERY_SPLIT split) {
		
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
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		
		return CommandUtil.format(pair, result, interval);
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
					endTime.getTime(), Inerval.INERVAL_15M.getDescption(),split);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public void openLong(FibInfo fibInfo, Klines afterLowKlines, List<Klines> klinesList_hit) {
		
		List<Klines> klinesList_hitBack = new ArrayList<Klines>();
		klinesList_hitBack.addAll(klinesList_hit);
		
		DeclineAndStrength<Boolean,QuotationMode> das = verifyDeclineAndStrength(klinesList_hitBack);
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		
		List<Klines> todayKlinesList = PriceUtil.getTodayKlines(klinesList_hit);
		
		Klines hitLowKlines = CollectionUtils.isEmpty(todayKlinesList) ? null : PriceUtil.getMinPriceKLine(todayKlinesList);
		
		//开盘、收盘、最低、最高价格
		double closePrice = Double.valueOf(hitKline.getClosePrice());
		//double openPrice = hitKline.getOpenPrice();
		double lowPrice = Double.valueOf(hitKline.getLowPrice());
		//double hightPrice = hitKline.getHighPrice();
		double currentPrice = closePrice;
		
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//多头行情做多 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 FIB0
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];
			
			//FibCode closePpositionCode = fibInfo.getTakeProfit(code);//止盈点位
			FibCode closePpositionCode = fibInfo.getTakeProfit(offset, codes);
			
			if(//PriceUtil.isLong(fibInfo.getFibValue(code), klinesList_hit)
					PriceUtil.checkFibHitPrice(klinesList_hit, fibInfo.getFibValue(code))
					&& !PriceUtil.isObsoleteLong(fibInfo,afterLowKlines,codes,offset)
					&& !PriceUtil.isObsoleteLong(fibInfo,hitLowKlines,codes,offset)) {//FIB1~startFibCode做多

				if(das.getVerify() && das.getLongOrShort() == QuotationMode.SHORT) {
					
					String subject = String.format("%s永续合约%s(%s)[%s]做多机会 %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice, closePpositionCode);
					
					text += "\r\n\r\n" + fibInfo.toString();
					
					String recEmail = userDetailsService.getFibMonitorUserEmail();
					sendEmail(subject,text,recEmail);
					
					//市价做多
					marketPlace(pair,PositionSide.LONG, 0, 0, offset, fibInfo, AutoTradeType.FIB_RET);
					
				}
				
				break;
			}
			
			if(code == fibInfo.getLevel().getStartFibCode()) {
				break;
			}
			
		}
	}

	@Override
	public void openShort(FibInfo fibInfo,Klines afterHighKlines,List<Klines> klinesList_hit) {
		
		List<Klines> klinesList_hitBack = new ArrayList<Klines>();
		klinesList_hitBack.addAll(klinesList_hit);
		
		DeclineAndStrength<Boolean,QuotationMode> das = verifyDeclineAndStrength(klinesList_hitBack);
		
		
		Klines hitKline = PriceUtil.getLastKlines(klinesList_hit);
		
		List<Klines> todayKlinesList = PriceUtil.getTodayKlines(klinesList_hit);

		Klines hitHighKlines = CollectionUtils.isEmpty(todayKlinesList) ? null : PriceUtil.getMaxPriceKLine(todayKlinesList);
		
		//开盘、收盘、最低、最高价格
		double closePrice = Double.valueOf(hitKline.getClosePrice());
		//double openPrice = hitKline.getOpenPrice();
		//double lowPrice = hitKline.getLowPrice();
		double hightPrice = Double.valueOf(hitKline.getHighPrice());
		double currentPrice = closePrice;
		
		String pair = hitKline.getPair();
		
		FibCode[] codes = FibCode.values();
		
		//空头行情做空 FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 FIB0
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];//当前斐波那契点位

			//FibCode closePpositionCode = fibInfo.getTakeProfit(code);//止盈点位
			FibCode closePpositionCode = fibInfo.getTakeProfit(offset, codes);
			
			if(//PriceUtil.isShort(fibInfo.getFibValue(code), klinesList_hit) 
					PriceUtil.checkFibHitPrice(klinesList_hit, fibInfo.getFibValue(code))
					&& !PriceUtil.isObsoleteShort(fibInfo,afterHighKlines,codes,offset)
					&& !PriceUtil.isObsoleteShort(fibInfo,hitHighKlines,codes,offset)) {
				
				if(das.getVerify() && das.getLongOrShort() == QuotationMode.LONG) {

					String subject = String.format("%s永续合约%s(%s)[%s]做空机会 %s", pair, code.getDescription(),
							PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
							fibInfo.getLevel().getLabel(),
							DateFormatUtil.format(new Date()));
					
					String text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice, closePpositionCode);

					text += "\r\n\r\n" + fibInfo.toString();
					
					String recEmail = userDetailsService.getFibMonitorUserEmail();
					sendEmail(subject,text,recEmail);
					
					//市价做空
					marketPlace(pair, PositionSide.SHORT, 0, 0, offset,  fibInfo, AutoTradeType.FIB_RET);
				}
				
				break;
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
						} else {
							
							//止盈点位
							FibCode takeProfitCode = FibCode.FIB618;
							
							if(autoTradeType == AutoTradeType.FIB_RET) {
								takeProfitCode = fibInfo.getTakeProfit(offset, codes);
							} else if(autoTradeType == AutoTradeType.EMA_INDEX) {
								takeProfitCode = FibCode.FIB618;
							} else if(autoTradeType == AutoTradeType.AREA_INDEX) {
								takeProfitCode = FibCode.FIB618;
							}
							
							stopLoss = new BigDecimal(
									PriceUtil.formatDoubleDecimal(PriceUtil.rectificationCutLossLongPrice_v3(Double.valueOf(priceInfo.getPrice()), u.getCutLoss()),decimalNum));
							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(takeProfitCode),decimalNum)
											);
						}
						
						//计算预计盈利百分比
						double profitPercent = PriceUtil.getRiseFluctuationPercentage(Double.valueOf(priceInfo.getPrice()),takeProfit.doubleValue());
						
						if(fibInfo != null) {
							//盈利太少则不做交易
							if((profitPercent * 100) < u.getProfit()) {
								logger.debug(pair + "预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%，不做交易");
								continue;
							}
						}
						
						List<BinanceOrderInfo> orderList = binanceRestTradeService.openOrders(binanceApiKey, binanceSecretKey, pair);
						if(!CollectionUtils.isEmpty(orderList)) {
							logger.debug("用户" + u.getUsername() + "在" + pair + "交易对中已有持仓");
							continue;
						}
						
						//最少下单数量
						String quantityNum = binanceWebsocketTradeService.getMarketMinQuantity(pair);
						
						BigDecimal quantity = new BigDecimal(quantityNum);
						quantity = quantity.multiply(new BigDecimal(u.getBaseStepSize()));

						SymbolConfig sc = binanceRestTradeService.getSymbolConfigBySymbol(binanceApiKey, binanceSecretKey, pair);

						int leverage = sc.getLeverage();
						
						//持仓价值
						double order_value = ( Double.valueOf(quantityNum) * Double.valueOf(priceInfo.getPrice()) ) / leverage;
						
						if(order_value > u.getPositionValue()) {
							logger.debug("最少下单数量仓位价值超过" + u.getPositionValue() + "USDT");
							continue;
						}
						
						String availableBalanceStr = binanceWebsocketTradeService.availableBalance(binanceApiKey, binanceSecretKey, "USDT");
						if(Double.valueOf(availableBalanceStr) < (order_value * 2)) {
							logger.debug("用户" + u.getUsername() + "可下单金额小于" + (order_value * 2) + "USDT");
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
						
						String subject_ = pair + "多头仓位已下单完成 " + dateStr;
						String text_ = StringUtil.formatLongMessage(pair, Double.valueOf(priceInfo.getPrice()), stopLoss.doubleValue(), 
								takeProfit.doubleValue(), decimalNum);
						
						text_ += "，预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%";
						
						if(fibInfo != null) {
							text_ += "\r\n" + fibInfo.toString();
						}
						
						sendEmail(subject_, text_, tradeUserEmail);
						
						//仓位统计
						TradingOrder to = new TradingOrder(pair, Double.valueOf(priceInfo.getPrice()), takeProfit.doubleValue(), stopLoss.doubleValue(), 
								new Date().getTime(), LongOrShortType.LONG.getValue(), order_value, quantity.doubleValue());
						orderRepository.insert(to);
						
						//Thread.sleep(500);
						
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
						} else {
							//止盈点位
							FibCode takeProfitCode = FibCode.FIB618;
							
							if(autoTradeType == AutoTradeType.FIB_RET) {
								takeProfitCode = fibInfo.getTakeProfit(offset, codes);
							} else if(autoTradeType == AutoTradeType.EMA_INDEX) {
								takeProfitCode = FibCode.FIB618;
							} else if(autoTradeType == AutoTradeType.AREA_INDEX) {
								takeProfitCode = FibCode.FIB618;
							}
							
							stopLoss = new BigDecimal(
									PriceUtil.formatDoubleDecimal(PriceUtil.rectificationCutLossShortPrice_v3(Double.valueOf(priceInfo.getPrice()), u.getCutLoss()), decimalNum)
											);
							takeProfit = new BigDecimal(
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(takeProfitCode),decimalNum)
											);
						}
						
						//计算预计盈利百分比
						double profitPercent = PriceUtil.getFallFluctuationPercentage(Double.valueOf(priceInfo.getPrice()),takeProfit.doubleValue());
						
						if(fibInfo != null) {
							//盈利太少则不做交易
							if((profitPercent * 100) < u.getProfit()) {
								logger.debug(pair + "预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%，不做交易");
								continue;
							}
						}

						List<BinanceOrderInfo> orderList = binanceRestTradeService.openOrders(binanceApiKey, binanceSecretKey, pair);
						if(!CollectionUtils.isEmpty(orderList)) {
							logger.debug("用户" + u.getUsername() + "在" + pair + "交易对中已有持仓");
							continue;
						}

						//最少下单数量
						String quantityNum = binanceWebsocketTradeService.getMarketMinQuantity(pair);
						
						BigDecimal quantity = new BigDecimal(quantityNum);
						quantity = quantity.multiply(new BigDecimal(u.getBaseStepSize()));

						SymbolConfig sc = binanceRestTradeService.getSymbolConfigBySymbol(binanceApiKey, binanceSecretKey, pair);

						int leverage = sc.getLeverage();
						
						//持仓价值 = 持仓价值 / 扛杆
						double order_value = ( Double.valueOf(quantityNum) * Double.valueOf(priceInfo.getPrice()) ) / leverage;
						
						if(order_value > u.getPositionValue()) {
							logger.debug("最少下单数量仓位价值超过" + u.getPositionValue() + "USDT");
							continue;
						}
						
						String availableBalanceStr = binanceWebsocketTradeService.availableBalance(binanceApiKey, binanceSecretKey, "USDT");
						if(Double.valueOf(availableBalanceStr) < (order_value * 2)) {
							logger.debug("用户" + u.getUsername() + "可下单金额小于" + (order_value * 2) + "USDT");
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
						
						String subject_ = pair + "空头仓位已下单完成 " + dateStr;
						String text_ = StringUtil.formatShortMessage(pair, Double.valueOf(priceInfo.getPrice()), takeProfit.doubleValue(), 
								stopLoss.doubleValue(), decimalNum);
						
						text_ += "，预计盈利：" + PriceUtil.formatDoubleDecimal(profitPercent * 100, 2) + "%";
						
						if(fibInfo != null) {
							text_ += "\r\n" + fibInfo.toString();
						}
						
						sendEmail(subject_, text_, tradeUserEmail);
						
						//仓位统计
						TradingOrder to = new TradingOrder(pair, Double.valueOf(priceInfo.getPrice()), takeProfit.doubleValue(), stopLoss.doubleValue(), 
								new Date().getTime(), LongOrShortType.SHORT.getValue(), order_value, quantity.doubleValue());
						orderRepository.insert(to);
						
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
			emailWorkTaskPool.add(new SendMailTask(subject, text, recEmail));
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

			text += "\r\n\r\n" + fibInfo.toString();
			
			String recEmail = userDetailsService.getFibMonitorUserEmail();
			sendEmail(subject,text,recEmail);
			
		}
	}

	@Override
	public void futuresHighOrLowMonitor(List<Klines> klinesList,List<Klines> klinesList_hit) {
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		Date now = new Date();

		Date lastDayEndTime = DateFormatUtil.getEndTime(DateFormatUtil.getHours(now.getTime()));
		
		FibKlinesData<List<Klines>,List<Klines>> fibKlinesData = PriceUtil.getFibKlinesData(klinesList);
		
		//标志性高点K线信息
		List<Klines> lconicHighPriceList = fibKlinesData.getLconicHighPriceList();
		//标志性低点K线信息
		List<Klines> lconicLowPriceList = fibKlinesData.getLconicLowPriceList();
		
		//昨日K线信息
		Klines lastDayKlines = PriceUtil.getLastKlines(klinesList);
		//最后一根k线
		Klines lastHitKlines = PriceUtil.getLastKlines(klinesList_hit);
		
		String pair = lastDayKlines.getPair();
		
		if(!lastDayKlines.isContains(lastHitKlines) && lastDayKlines.isDownlead()){
			lconicLowPriceList.add(lastDayKlines);
		}
		
		if(!lastDayKlines.isContains(lastHitKlines) && lastDayKlines.isUplead()){
			lconicHighPriceList.add(lastDayKlines);
		}
		
		//排序 按开盘时间升序 从旧到新
		lconicHighPriceList.sort(kc);
		lconicLowPriceList.sort(kc);
		
		highOrLowHitPriceRepository.remove(pair, lastDayEndTime.getTime());
		
		List<HighOrLowHitPrice> priceList = highOrLowHitPriceRepository.find(pair);
		
		HighOrLowHitPrice todayHitLowPrice = PriceUtil.getMin(priceList);
		HighOrLowHitPrice todayHitHighPrice = PriceUtil.getMax(priceList);
		
		Klines hitLowKlines = PriceUtil.getPositionLowKlines(lconicLowPriceList, klinesList_hit);
		Klines hitHighKlines = PriceUtil.getPositionHighKlines(lconicHighPriceList, klinesList_hit);
		
		String dateStr = DateFormatUtil.format(now);
		
		String subject = "";
		String text = "";
		
		String lastDayStr = "";
		
		if(!ObjectUtils.isEmpty(hitLowKlines)) {
			
			double lowPrice = Double.valueOf(hitLowKlines.getLowPrice());
			
			HighOrLowHitPrice price = new HighOrLowHitPrice(pair, lowPrice, now.getTime());
			highOrLowHitPriceRepository.insert(price);
			
			if(lastDayKlines.isEquals(hitLowKlines)) {
				lastDayStr = "昨日最低价";
			}
			
			if(PriceUtil.isLong(lowPrice, klinesList_hit) && (todayHitLowPrice == null || todayHitLowPrice.getPrice() >= lowPrice)) {
				
				subject = String.format("%s永续合约跌破%s(%s)并收回 %s", pair,lastDayStr,PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()),dateStr);
				
				text = String.format("%s永续合约跌破(%s)最低价(%s)并收回", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(hitLowKlines.getStartTime())), 
						PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()));
			} else if(PriceUtil.isShort(lowPrice, klinesList_hit) && (todayHitLowPrice == null || todayHitLowPrice.getPrice() >= lowPrice)
				&& ("BTCUSDT".equals(pair) || "ETHUSDT".equals(pair))) {
				
				subject = String.format("%s永续合约跌破%s(%s) %s", pair,lastDayStr,PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()),dateStr);
				
				text = String.format("%s永续合约跌破(%s)最低价(%s)", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(hitLowKlines.getStartTime())), 
						PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()));
			}
		
		} else if(!ObjectUtils.isEmpty(hitHighKlines)) {
			
			if(lastDayKlines.isEquals(hitHighKlines)) {
				lastDayStr = "昨日最高价";
			}
			
			double highPrice = Double.valueOf(hitHighKlines.getHighPrice());
			
			HighOrLowHitPrice price = new HighOrLowHitPrice(pair, highPrice, now.getTime());
			highOrLowHitPriceRepository.insert(price);
			
			if(PriceUtil.isLong(highPrice, klinesList_hit) && (todayHitHighPrice == null || todayHitHighPrice.getPrice() <= highPrice)
				&& ("BTCUSDT".equals(pair) || "ETHUSDT".equals(pair))) {
				
				subject = String.format("%s永续合约突破%s(%s) %s", pair,lastDayStr,PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()),dateStr);
				
				text = String.format("%s永续合约突破(%s)最高价(%s)", pair, 
						DateFormatUtil.format_yyyy_mm_dd(new Date(hitHighKlines.getStartTime())), 
						PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()));
				
			} else if(PriceUtil.isShort(highPrice, klinesList_hit) && (todayHitHighPrice == null || todayHitHighPrice.getPrice() <= highPrice)) {
				
				subject = String.format("%s永续合约突破%s(%s)并收回 %s", pair,lastDayStr,PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()),dateStr);
				
				text = String.format("%s永续合约突破(%s)最高价(%s)并收回", pair,
						DateFormatUtil.format_yyyy_mm_dd(new Date(hitHighKlines.getStartTime())), 
						PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()));
				
			}
			
		}
		
		String recEmail = userDetailsService.getHighOrLowMonitorUserEmail();
	 	sendEmail(subject, text, recEmail);
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
			
			//sendFib0Email(fibInfo, klinesList_hit);
			
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
			
			//sendFib0Email(secondFibInfo, klinesList_hit);
			
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
		
		//sendFib0Email(thirdFibInfo, klinesList_hit);
	}
	
	@Override
	public void futuresEMAMonitor(List<Klines> klinesList) {
		if(!CollectionUtils.isEmpty(klinesList)){
			PriceUtil.calculateEMAArray(klinesList, EMAType.EMA7);
			PriceUtil.calculateEMAArray(klinesList, EMAType.EMA25);
			PriceUtil.calculateEMAArray(klinesList, EMAType.EMA99);

			Klines lastKlines = PriceUtil.getLastKlines(klinesList);
			String pair = lastKlines.getPair();
			int decimalNum = lastKlines.getDecimalNum();

			String subject = "";
			String text = "";
			String dateStr = DateFormatUtil.format(new Date());

			if(PriceUtil.isOpenLongEMA(klinesList)){
				subject = String.format("%s永续合约做多机会 %s", pair, dateStr);
			} else if(PriceUtil.isOpenShortEMA(klinesList)){
				subject = String.format("%s永续合约做空机会 %s", pair, dateStr);
			}

			text = lastKlines.toString() + "\n\n";
		 	text += String.format("EMA7: %s, EMA25: %s, EMA99: %s ", PriceUtil.formatDoubleDecimal(lastKlines.getEma7(), decimalNum),
		 			PriceUtil.formatDoubleDecimal(lastKlines.getEma25(), decimalNum),
		 			PriceUtil.formatDoubleDecimal(lastKlines.getEma99(), decimalNum));
			
		 	String recEmail = userDetailsService.getEmaMonitorUserEmail();
		 	sendEmail(subject, text, recEmail);
		}
	}
	
	@Override
	public void declineAndStrengthCheck(List<Klines> klinesListData) {
		
		List<Klines> klinesList = new ArrayList<Klines>();
		klinesList.addAll(klinesListData);
		
		if(!CollectionUtils.isEmpty(klinesList)){
			
			int lastIndex = klinesList.size() - 1;
			Klines lastKlines = klinesList.remove(lastIndex);
			if(CollectionUtils.isEmpty(klinesList)) {
				return;
			}
			
			PriceFibInfo info = PriceUtil.getPriceFibInfo(klinesList);
			
			Klines currentKlines = PriceUtil.getLastKlines(klinesList);

			String pair = currentKlines.getPair();
					
			String percentageStr = PriceUtil.formatDoubleDecimal(PriceUtil.getPriceFluctuationPercentage(klinesList), 2);
			
			double pricePercentage = Double.valueOf(percentageStr);
			
			String text = "";//邮件内容
			String subject = "";//邮件主题
			String dateStr = DateFormatUtil.format(new Date());
			
			boolean isFall = false;
			
			if(PriceUtil.isFall(klinesList)) {//下跌
				isFall = true;
				if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT") || pair.equals("BNBUSDT")) {
					if(pricePercentage >= 5) {
						text = pair + "永续合约价格大暴跌";
					} else if(pricePercentage >= 3) {
						text = pair + "永续合约价格暴跌";
					}else if(pricePercentage >= 1.5) {
						text = pair + "永续合约价格大跌";
					}
				} else {
					if(pricePercentage >= 15) {
						text = pair + "永续合约价格大暴跌";
					} else if(pricePercentage >= 10) {
						text = pair + "永续合约价格暴跌";
					}else if(pricePercentage >= 5) {
						text = pair + "永续合约价格大跌";
					}
				}
				
			} else if(PriceUtil.isRise(klinesList)) {
				if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT") || pair.equals("BNBUSDT")) {
					if(pricePercentage >= 5) {
						text = pair + "永续合约价格大暴涨";
					} else if(pricePercentage >= 3) {
						text = pair + "永续合约价格暴涨";
					}else if(pricePercentage >= 1.5) {
						text = pair + "永续合约价格大涨";
					}
				} else {
					if(pricePercentage >= 15) {
						text = pair + "永续合约价格大暴涨";
					} else if(pricePercentage >= 10) {
						text = pair + "永续合约价格暴涨";
					}else if(pricePercentage >= 5) {
						text = pair + "永续合约价格大涨";
					}
				}
			}
			
			if(StringUtil.isNotEmpty(text)) {
				double bodyHighPrice = 0;
				double bodyLowPrice = 0;
				
				if(currentKlines.isFall()) {
					bodyHighPrice = Double.valueOf(currentKlines.getOpenPrice());
					bodyLowPrice = Double.valueOf(currentKlines.getClosePrice());
				} else {
					bodyLowPrice = Double.valueOf(currentKlines.getOpenPrice());
					bodyHighPrice = Double.valueOf(currentKlines.getClosePrice());
				}
				
				//下跌情况
				if(isFall) {
					//看涨吞没
					if(Double.valueOf(lastKlines.getClosePrice()) >= bodyHighPrice) {
						subject = pair + "永续合约强势价格行为 " + dateStr;
					} else //前一根k线为阳线 当前k线为阴线且收盘价未创出新低 
					if(currentKlines.isRise() && lastKlines.isFall() && Double.valueOf(lastKlines.getClosePrice()) >= Double.valueOf(currentKlines.getLowPrice())) {
						subject = pair + "永续合约强势价格行为 " + dateStr;
					}
				} else {//上涨情况
					//看跌吞没
					if(Double.valueOf(lastKlines.getClosePrice()) <= bodyLowPrice) {
						subject = pair + "永续合约颓势价格行为 " + dateStr;
					} else //前一根k线为阴线 当前k线为阳线且收盘价未创出新高
					if(currentKlines.isFall() && lastKlines.isRise() && Double.valueOf(lastKlines.getClosePrice()) <= Double.valueOf(currentKlines.getHighPrice())) {
						subject = pair + "永续合约颓势价格行为 " + dateStr;
					}
				}
				
				if(StringUtil.isNotEmpty(subject)) {
					
					
					double currentPrice = Double.valueOf(lastKlines.getClosePrice());
					
					Klines lowKlines = klinesRepository.findOneByStartTime(info.getLowKlinesTime(), pair, info.getInerval());
					Klines highKlines = klinesRepository.findOneByStartTime(info.getHighKlinesTime(), pair, info.getInerval());
					double lowPrice = Double.valueOf(lowKlines.getLowPrice());
					double highPrice = Double.valueOf(highKlines.getHighPrice());
					
					FibInfo fib = new FibInfo(lowKlines, highKlines, highKlines.getDecimalNum(), FibLevel.LEVEL_1);
					
					text += pricePercentage + "%" + "\r\n" + fib.toString();
					
					String recEmail = userDetailsService.getEmaMonitorUserEmail();
					/*
					if(info.verifyData(currentPrice, fib)) {
						sendEmail(subject, text, recEmail);
					}*/
					
					double percent = PriceUtil.getRiseFluctuationPercentage(currentPrice, fib.getFibValue(FibCode.FIB618));
					String percentStr = PriceUtil.formatDoubleDecimal(percent * 100, 2);
					
					if(fib.getQuotationMode() == QuotationMode.SHORT && info.verifyData(currentPrice, fib)) {
						
						subject = String.format("%s永续合约强势价格行为(PNL:%s%%) %s", pair, percentStr, dateStr);
						
						text = StringUtil.formatLongMessage(pair, currentPrice, PriceUtil.rectificationCutLossLongPrice(lowPrice), fib.getFibValue(FibCode.FIB618), lastKlines.getDecimalNum());
						
						text += "，预计盈利：" + percentStr + "%";
						
						sendEmail(subject, text, recEmail);
						
						//市价做多
						marketPlace(pair, PositionSide.LONG, 0, 0, 0, fib, AutoTradeType.PRICE_ACTION);
						
					} else if(fib.getQuotationMode() == QuotationMode.LONG && info.verifyData(currentPrice, fib)) {
						
						subject = String.format("%s永续合约颓势价格行为(PNL:%s%%) %s", pair, percentStr, dateStr);
						
						text = StringUtil.formatShortMessage(pair, currentPrice, fib.getFibValue(FibCode.FIB618), PriceUtil.rectificationCutLossShortPrice(highPrice), lastKlines.getDecimalNum());

						text += "，预计盈利：" + percentStr + "%";
						
						sendEmail(subject, text, recEmail);
						
						//市价做空
						marketPlace(pair, PositionSide.SHORT, 0, 0, 0, fib, AutoTradeType.PRICE_ACTION);
					}
				}
			}
		}
	}
	
	@Override
	public DeclineAndStrength<Boolean,QuotationMode> verifyDeclineAndStrength(List<Klines> klinesList) {
		boolean bol = false;
		QuotationMode qm = null;
		if(!CollectionUtils.isEmpty(klinesList)){
			int lastIndex = klinesList.size() - 1;
			Klines lastKlines = klinesList.remove(lastIndex);
			if(CollectionUtils.isEmpty(klinesList)) {
				return new DeclineAndStrength<Boolean,QuotationMode>(bol,qm);
			}
			
			Klines currentKlines = PriceUtil.getLastKlines(klinesList);

			String pair = currentKlines.getPair();
					
			String percentageStr = PriceUtil.formatDoubleDecimal(PriceUtil.getPriceFluctuationPercentage(klinesList), 2);
			
			double pricePercentage = Double.valueOf(percentageStr);
			
			String text = "";//邮件内容
			String subject = "";//邮件主题
			String dateStr = DateFormatUtil.format(new Date());
			
			boolean isFall = false;
			
			if(PriceUtil.isFall(klinesList)) {//下跌
				isFall = true;
				if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT") || pair.equals("BNBUSDT")) {
					if(pricePercentage >= 5) {
						text = pair + "永续合约价格大暴跌";
					} else if(pricePercentage >= 3) {
						text = pair + "永续合约价格暴跌";
					}else if(pricePercentage >= 1.5) {
						text = pair + "永续合约价格大跌";
					}
				} else {
					if(pricePercentage >= 15) {
						text = pair + "永续合约价格大暴跌";
					} else if(pricePercentage >= 10) {
						text = pair + "永续合约价格暴跌";
					}else if(pricePercentage >= 5) {
						text = pair + "永续合约价格大跌";
					}
				}
				
			} else if(PriceUtil.isRise(klinesList)) {
				if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT") || pair.equals("BNBUSDT")) {
					if(pricePercentage >= 5) {
						text = pair + "永续合约价格大暴涨";
					} else if(pricePercentage >= 3) {
						text = pair + "永续合约价格暴涨";
					}else if(pricePercentage >= 1.5) {
						text = pair + "永续合约价格大涨";
					}
				} else {
					if(pricePercentage >= 15) {
						text = pair + "永续合约价格大暴涨";
					} else if(pricePercentage >= 10) {
						text = pair + "永续合约价格暴涨";
					}else if(pricePercentage >= 5) {
						text = pair + "永续合约价格大涨";
					}
				}
			}
			
			if(StringUtil.isNotEmpty(text)) {
				double bodyHighPrice = 0;
				double bodyLowPrice = 0;
				
				if(currentKlines.isFall()) {
					bodyHighPrice = Double.valueOf(currentKlines.getOpenPrice());
					bodyLowPrice = Double.valueOf(currentKlines.getClosePrice());
				} else {
					bodyLowPrice = Double.valueOf(currentKlines.getOpenPrice());
					bodyHighPrice = Double.valueOf(currentKlines.getClosePrice());
				}
				
				//下跌情况
				if(isFall) {
					qm = QuotationMode.SHORT;
					//看涨吞没
					if(Double.valueOf(lastKlines.getClosePrice()) >= bodyHighPrice) {
						subject = pair + "永续合约强势价格行为 " + dateStr;
					} else //前一根k线为阳线 当前k线为阴线且收盘价未创出新低 
					if(currentKlines.isRise() && lastKlines.isFall() && Double.valueOf(lastKlines.getClosePrice()) >= Double.valueOf(currentKlines.getLowPrice())) {
						subject = pair + "永续合约强势价格行为 " + dateStr;
					}
				} else {//上涨情况
					qm = QuotationMode.LONG;
					//看跌吞没
					if(Double.valueOf(lastKlines.getClosePrice()) <= bodyLowPrice) {
						subject = pair + "永续合约颓势价格行为 " + dateStr;
					} else //前一根k线为阴线 当前k线为阳线且收盘价未创出新高
					if(currentKlines.isFall() && lastKlines.isRise() && Double.valueOf(lastKlines.getClosePrice()) <= Double.valueOf(currentKlines.getHighPrice())) {
						subject = pair + "永续合约颓势价格行为 " + dateStr;
					}
				}
				
				if(StringUtil.isNotEmpty(subject)) {
					bol = true;
				}
			}
		}
		return new DeclineAndStrength<Boolean,QuotationMode>(bol,qm);
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
	
	/**
	 * 判断上涨
	 * @param lastKlines
	 * @param parentKlines
	 * @return 
	 */
	/*private boolean checkEmaRise(Klines lastKlines,Klines parentKlines) {
		//k线主体开盘价或收盘价 最高价最低价 
		double parentBodyHighPrice = 0;
		double parentBodyLowPrice = 0;
		double parentHighPrice = parentKlines.getHighPrice();
		double parentLowPrice = parentKlines.getLowPrice();
		if(parentKlines.isFall()) {
			parentBodyHighPrice = parentKlines.getOpenPrice();
			parentBodyLowPrice = parentKlines.getClosePrice();
		} else if(parentKlines.isRise()) {
			parentBodyLowPrice = parentKlines.getOpenPrice();
			parentBodyHighPrice = parentKlines.getClosePrice();
		}
		
		return parentKlines.getEma7() < parentKlines.getEma25() && parentKlines.getEma25() < parentKlines.getEma99() 
				&& parentBodyHighPrice < parentKlines.getEma7() && lastKlines.getClosePrice() > parentBodyHighPrice;
	}*/
	
	/**
	 * 判断下跌
	 * @param lastKlines
	 * @param parentKlines
	 * @return
	 */
	/*private boolean checkEmaFall(Klines lastKlines,Klines parentKlines) {
		//k线主体开盘价或收盘价 最高价最低价 
		double parentBodyHighPrice = 0;
		double parentBodyLowPrice = 0;
		double parentHighPrice = parentKlines.getHighPrice();
		double parentLowPrice = parentKlines.getLowPrice();
		if(parentKlines.isFall()) {
			parentBodyHighPrice = parentKlines.getOpenPrice();
			parentBodyLowPrice = parentKlines.getClosePrice();
		} else if(parentKlines.isRise()) {
			parentBodyLowPrice = parentKlines.getOpenPrice();
			parentBodyHighPrice = parentKlines.getClosePrice();
		}
		return parentKlines.getEma7() > parentKlines.getEma25() && parentKlines.getEma25() > parentKlines.getEma99() 
				&& parentBodyLowPrice > parentKlines.getEma7() && lastKlines.getClosePrice() < parentBodyLowPrice;
	}*/

	@Override
	public void futuresEmaRiseAndFall(List<Klines> list) {
		
		List<Klines> klinesList = new ArrayList<Klines>();
		klinesList.addAll(list);
		
		KlinesComparator kc_asc = new KlinesComparator(SortType.ASC);
		klinesList.sort(kc_asc);
		
		PriceUtil.calculateEMAArray(klinesList, EMAType.EMA7);
		PriceUtil.calculateEMAArray(klinesList, EMAType.EMA25);
		PriceUtil.calculateEMAArray(klinesList, EMAType.EMA99);
		
		KlinesUtil ku = new KlinesUtil(klinesList);
		Klines lastKlines = ku.removeLast();
		//Klines parentKlines = ku.removeLast();
		//Klines parentNextKlines = ku.removeLast();
		
		String pair = lastKlines.getPair();
		
		//int decimalNum = lastKlines.getDecimalNum();
		
		String subject = "";
		String text = "";
		String dateStr = DateFormatUtil.format(new Date());
		//金叉
		/*
		//开始上涨
		if(parentKlines.getEma7() < parentKlines.getEma25() && 
				lastKlines.getEma7() >= lastKlines.getEma25()) {
			subject = String.format("%s永续合约开始上涨 %s", pair, dateStr);
		}
		//开始下跌
		else if(parentKlines.getEma7() > parentKlines.getEma25() && 
				lastKlines.getEma7() <= lastKlines.getEma25()) {
			subject = String.format("%s永续合约开始下跌 %s", pair, dateStr);
		}
		
		text = lastKlines.toString() + "\n\n";
		text += String.format("EMA7: %s, EMA25: %s, EMA99: %s ", PriceUtil.formatDoubleDecimal(lastKlines.getEma7(), decimalNum),
				PriceUtil.formatDoubleDecimal(lastKlines.getEma25(), decimalNum),
				PriceUtil.formatDoubleDecimal(lastKlines.getEma99(), decimalNum));
		
		String recEmail = userDetailsService.getEmaRiseAndFallUserEmail();
		
	 	sendEmail(subject, text, recEmail);
	 	*/
	 	double closePrice = Double.valueOf(lastKlines.getClosePrice());
	 	double lowPrice = Double.valueOf(lastKlines.getLowPrice());
	 	double highPrice = Double.valueOf(lastKlines.getHighPrice());
	 	double ema99 = lastKlines.getEma99();
	 	
	 	String recEmail = userDetailsService.getEmaRiseAndFallUserEmail();
	 	
	 	//判断回踩ema99 情况
	 	if((closePrice >= ema99 && lowPrice <= ema99) || (closePrice <= ema99 && highPrice >= ema99)) {
	 		FibInfo fibInfo = PriceUtil.getFibInfoForEma(klinesList);
	 		if(fibInfo != null) {
	 			
	 			double percent = PriceUtil.getRiseFluctuationPercentage(closePrice, fibInfo.getFibValue(FibCode.FIB618));
				String percentStr = PriceUtil.formatDoubleDecimal(percent * 100, 2);
				
		 		QuotationMode mode = fibInfo.getQuotationMode();
		 		
		 		if(mode == QuotationMode.LONG) {
		 			
		 			subject = String.format("%s永续合约做空交易机会(PNL:%s%%) %s", pair, percentStr, dateStr);
					
					text = StringUtil.formatShortMessage(pair, closePrice, fibInfo.getFibValue(FibCode.FIB618), PriceUtil.rectificationCutLossShortPrice(highPrice), lastKlines.getDecimalNum());

					text += "，预计盈利：" + percentStr + "%";
					
					text += "\r\n" + fibInfo.toString();
					
					sendEmail(subject, text, recEmail);
					
		 			marketPlace(pair, PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.EMA_INDEX);
		 		} else {
		 			
		 			subject = String.format("%s永续合约做多交易机会(PNL:%s%%) %s", pair, percentStr, dateStr);
					
					text = StringUtil.formatLongMessage(pair, closePrice, PriceUtil.rectificationCutLossLongPrice(lowPrice), fibInfo.getFibValue(FibCode.FIB618), lastKlines.getDecimalNum());
					
					text += "，预计盈利：" + percentStr + "%";
					
					text += "\r\n" + fibInfo.toString();
					
					sendEmail(subject, text, recEmail);
		 			
		 			marketPlace(pair, PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.EMA_INDEX);
		 		}
	 		}
	 	}
	}
	
	@Override
	public void futuresConsolidationAreaMonitor(List<Klines> klinesList, List<Klines> hitKlinesList) {
		String dateStr = DateFormatUtil.format(new Date());
		ConsolidationAreaUtil cau = new ConsolidationAreaUtil(klinesList);
		//获取盘整区信息
		ConsolidationArea area =cau.getConsolidationArea();
		if(area.isEmpty()) {
			return;
		}
		
		logger.debug(area);
		//最后一根k线
		Klines lastDay = PriceUtil.getLastKlines(klinesList);
		
		double areaHighPrice = area.getHighPriceDoubleValue();
		double areaLowPrice = area.getLowPriceDoubleValue();
		
		Klines current = PriceUtil.getLastKlines(hitKlinesList);
		String pair = current.getPair();
		double hightPrice = current.getHighPriceDoubleValue();
		double lowPrice = current.getLowPriceDoubleValue();
		//double openPrice = current.getOpenPriceDoubleValue();
		double closePrice = current.getClosePriceDoubleValue();
		
		PriceInfo priceInfo = binanceWebsocketTradeService.getPrice(pair);
		if(priceInfo == null) {
			return;
		}
		
		String subject = "";
		String text = "";
		
		String recEmail = userDetailsService.getAreaMonitorUserEmail();
		
		//订阅信息
		if(hitPrice(current, areaLowPrice)) {
			subject = String.format("%s永续合约价格已到达盘整区下边缘%s %s", pair, areaLowPrice, dateStr);
			text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
					pair,
					PriceUtil.formatDoubleDecimal(areaLowPrice,current.getDecimalNum()),
					PriceUtil.formatDoubleDecimal(areaHighPrice,current.getDecimalNum()),
					priceInfo.getPrice());
			sendEmail(subject, text, recEmail);
		} 
		
		if(hitPrice(current, areaHighPrice)) {
			subject = String.format("%s永续合约价格已到达盘整区上边缘%s %s", pair, areaHighPrice, dateStr);
			text = String.format("%s永续合约盘整区价格区间%s~%s，当前价格：%s", 
					pair,
					PriceUtil.formatDoubleDecimal(areaLowPrice,current.getDecimalNum()),
					PriceUtil.formatDoubleDecimal(areaHighPrice,current.getDecimalNum()),
					priceInfo.getPrice());
			sendEmail(subject, text, recEmail);
		}
		
		//盘整区之后开始时间
		Date startTime = DateFormatUtil.getStartTimeBySetDay(new Date(area.getEndKlinesStartTime()), 1);
		
		//反弹测试盘整区底部情况 做空
		if(hitPrice(current, areaLowPrice) && area.isLte(lastDay)) {
			//盘整区之后的所有15分钟级别k线信息
			List<Klines> list_15m = klinesRepository.findByPairAndGtStartTime(pair, startTime.getTime(), Inerval.INERVAL_15M.getDescption());
			Klines high = PriceUtil.getMaxPriceKLine(list_15m);
			Klines low = PriceUtil.getMinPriceKLine(list_15m);
			//从低到高拉斐波那契回撤
			FibInfo fibInfo = new FibInfo(low.getLowPriceDoubleValue(), high.getHighPriceDoubleValue(), current.getDecimalNum(), FibLevel.LEVEL_1);
			marketPlace(pair, PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.AREA_INDEX);
		} else 
		//做多
		if(closePrice >= areaLowPrice && lowPrice <= areaLowPrice) {
			FibInfo fibInfo = new FibInfo(areaHighPrice, areaLowPrice, current.getDecimalNum(), FibLevel.LEVEL_1);
			marketPlace(pair, PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.AREA_INDEX);
		}
		
		//回踩盘整区上方情况 做多
		if(hitPrice(current, areaHighPrice) && area.isGte(lastDay)) {
			//盘整区之后的所有15分钟级别k线信息
			List<Klines> list_15m = klinesRepository.findByPairAndGtStartTime(pair, startTime.getTime(), Inerval.INERVAL_15M.getDescption());
			Klines high = PriceUtil.getMaxPriceKLine(list_15m);
			Klines low = PriceUtil.getMinPriceKLine(list_15m);
			//从高到低拉斐波那契回撤
			FibInfo fibInfo = new FibInfo(high.getHighPriceDoubleValue(), low.getLowPriceDoubleValue(), current.getDecimalNum(), FibLevel.LEVEL_1);
			marketPlace(pair, PositionSide.LONG, 0, 0, 0, fibInfo, AutoTradeType.AREA_INDEX);
		} else 
		//做空
		if(closePrice <= areaHighPrice && hightPrice >= areaHighPrice) {
			FibInfo fibInfo = new FibInfo(areaLowPrice, areaHighPrice, current.getDecimalNum(), FibLevel.LEVEL_1);
			marketPlace(pair, PositionSide.SHORT, 0, 0, 0, fibInfo, AutoTradeType.AREA_INDEX);
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
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));
				
				//所有k线信息
				List<Klines> list = klinesRepository.findByPairAndGtStartTime(info.getSymbol(), time * 1000, Inerval.INERVAL_15M.getDescption());
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
				
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));

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
				
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));
				
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
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));
				
				long startTime = time0 < time1 ? (time0 * 1000) : (time1 * 1000);
				long endTime = time0 < time1 ? (time1 * 1000) : (time0 * 1000);
				
				List<Klines> list = klinesRepository.findByPairAndGtStartTime(info.getSymbol(), endTime, Inerval.INERVAL_15M.getDescption());
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
				
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));
				
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
				
				emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));
				
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
				
				this.emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));
				
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
				this.emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));
				
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
				this.emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));
				
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
					this.emailWorkTaskPool.add(new SendMailTask(subject, text, info.getOwner()));
				}
			}
		}
	}

	@Override
	public void closeOrder(Klines klines) {
		List<TradingOrder> orderList = orderRepository.findAll(klines.getPair(), 0, -1, 0);
		if(!CollectionUtils.isEmpty(orderList)) {
			for(TradingOrder o : orderList) {
				logger.debug(o);
				double sl = o.getStopLoss();//止损
				double tp = o.getTakeProfit();//止盈
				LongOrShortType type = LongOrShortType.resolve(o.getLongOrShort());//持仓方向
				//空头平仓
				if(type == LongOrShortType.SHORT) {
					double closePrice = 0;//平仓价
					//止损 收盘价或最高价大于止损价
					if(Double.valueOf(klines.getClosePrice()) >= sl || Double.valueOf(klines.getHighPrice()) >= sl) {
						closePrice = Double.valueOf(klines.getClosePrice());
					} else//止盈 收盘价或最低价小于止盈价
					if(Double.valueOf(klines.getClosePrice()) <= tp || Double.valueOf(klines.getLowPrice()) <= tp){
						closePrice = o.getTakeProfit();
					}
					if(closePrice > 0) {//平仓
						//空头盈亏金额 = （开仓价 - 平仓价） * 持仓数量
						double pnl = (o.getOpenPrice() - closePrice) * o.getCoinSize();
						orderRepository.updateById(o.getId(), closePrice, new Date().getTime(), pnl);
					}
				} else if(type == LongOrShortType.LONG) {
					double closePrice = 0;//平仓价
					//止损 收盘价或最低价小于止损价
					if(Double.valueOf(klines.getClosePrice()) <= sl || Double.valueOf(klines.getHighPrice()) <= sl) {
						closePrice = Double.valueOf(klines.getClosePrice());
					} else//止盈 收盘价或最高价大于止盈价
					if(Double.valueOf(klines.getClosePrice()) >= tp || Double.valueOf(klines.getLowPrice()) >= tp){
						closePrice = o.getTakeProfit();
					}
					if(closePrice > 0) {//平仓
						//多头盈亏金额 = （平仓价 - 开仓价） * 持仓数量
						double pnl = (o.getOpenPrice() - closePrice) * o.getCoinSize();
						orderRepository.updateById(o.getId(), closePrice, new Date().getTime(), pnl);
					}
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
                	result = false;
                	List<Klines> data = continuousKlines(current.getPair(), startTime, endTime, current.getInterval(), QUERY_SPLIT.NOT_ENDTIME);
                	logger.debug(current.getPair() + "交易对" + current.getInterval() + "级别k线信息数据有缺矢，已同步" + data.size() 
                				+ "条数据，缺失时间段：" + DateFormatUtil.format(startTime) + " ~ " + DateFormatUtil.format(endTime));
                	klinesRepository.insert(data);
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
                    logger.debug("查询到重复K线信息：" + current);
                    result = false;
                    String _id = current.getId();
                    if(StringUtil.isNotEmpty(_id)){
                    	klinesRepository.remove(_id);
                        logger.debug("重复k线已从数据库中删除");
                    }
                }
            }
            
        }
        return result;
    }

	@Override
	public String getClosePrice(String pair, Inerval inerval) {
		String price = "0";
		List<Klines> list = klinesRepository.findLastKlinesByPair(pair, inerval.getDescption(), 1);
		if(!CollectionUtils.isEmpty(list)) {
			price = list.get(0).getClosePrice();
		}
		return price;
	}
}
