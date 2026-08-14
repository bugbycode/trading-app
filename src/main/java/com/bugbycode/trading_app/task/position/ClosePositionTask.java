package com.bugbycode.trading_app.task.position;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.binance.module.position.PositionInfo;
import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.binance.trade.websocket.impl.BinanceWebsocketTradeServiceImpl;
import com.bugbycode.config.AppConfig;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.TradeStepBackStatus;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.user.User;
import com.bugbycode.service.user.UserService;
import com.bugbycode.websocket.trading.endpoint.TradingWebSocketClientEndpoint;

/**
 * 自动关闭现有持仓
 */
public class ClosePositionTask implements Runnable{

	private final Logger logger = LogManager.getLogger(ClosePositionTask.class);

	private String pair;//要关闭的交易对
	
	private PositionSide ps;//持仓方向
	
	private AutoTradeType autoTradeType;//技术指标
	
	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	private UserService userDetailsService;
	
	private TradingWebSocketClientEndpoint websocketApi;
	
	private User user;
	
	private List<User> users;
	
	private OpenPrice price;
	
	/**
	 * 关闭用户持仓任务
	 * @param pair 交易对
	 * @param ps 要关闭的持仓方向 LONG/SHORT
	 * @param user 相关用户
	 * @param price 触发的价格信息
	 */
	public ClosePositionTask(String pair, PositionSide ps, User user, OpenPrice price) {
		this.pair = pair;
		this.ps = ps;
		this.user = user;
		this.price = price;
	}
	
	/**
	 * 关闭用户持仓任务
	 * @param pair 交易对
	 * @param ps 要关闭的持仓方向 LONG/SHORT
	 * @param users 相关用户
	 * @param price 触发的价格信息
	 */
	public ClosePositionTask(String pair, PositionSide ps, List<User> users, OpenPrice price) {
		this.pair = pair;
		this.ps = ps;
		this.users = users;
		this.price = price;
	}
	
	/**
	 * 关闭用户持仓任务
	 * @param pair 交易对
	 * @param ps 要关闭的持仓方向 LONG/SHORT
	 * @param autoTradeType 指标
	 * @param userDetailsService
	 * @param price 触发的价格信息
	 */
	public ClosePositionTask(String pair, PositionSide ps, AutoTradeType autoTradeType,
			UserService userDetailsService, OpenPrice price) {
		this.pair = pair;
		this.ps = ps;
		this.autoTradeType = autoTradeType;
		this.userDetailsService = userDetailsService;
		this.price = price;
	}

	@Override
	public void run() {
		try {
			this.websocketApi = new TradingWebSocketClientEndpoint(AppConfig.WEBSOCKET_API_URL);
			this.binanceWebsocketTradeService = new BinanceWebsocketTradeServiceImpl(this.websocketApi);
			
			List<User> userList = new ArrayList<User>();
			if(user == null && userDetailsService != null) {
				userList = userDetailsService.queryByAutoTrade(AutoTrade.OPEN, autoTradeType);
			} else if(user != null) {
				userList.add(user);
			} else if(!CollectionUtils.isEmpty(users)) {
				userList.addAll(users);
			}
			
			for(User u : userList) {
				String binanceApiKey = u.getBinanceApiKey();
				String binanceSecretKey = u.getBinanceSecretKey();
				
				if(price != null && (autoTradeType == AutoTradeType.FIB_RET || autoTradeType == AutoTradeType.PRICE_ACTION)) {
					
					FibCode code = price.getCode();
					
					if(code.lt(u.getFibLevelType().getLevelCode())) {//回撤限制
						continue;
					}
					
					//回踩单判断
					TradeStepBackStatus tradeStepBackStatus = TradeStepBackStatus.valueOf(u.getTradeStepBack());
					if(code.gt(FibCode.FIB1) && tradeStepBackStatus == TradeStepBackStatus.CLOSE) {
						continue;
					}
				}
				
				List<PositionInfo> positionList = binanceWebsocketTradeService.getPositionInfo(binanceApiKey, binanceSecretKey, pair, ps);
				logger.debug("共查询到{}交易对共{}个仓位", pair, positionList.size());
				for(PositionInfo p : positionList) {
					com.bugbycode.module.Result<BinanceOrderInfo, RuntimeException> excute_rs = binanceWebsocketTradeService.closePositionInfo(binanceApiKey, binanceSecretKey, p);
					if(excute_rs.getErr() != null) {
						//throw new RuntimeException("关闭" + pair + "空头仓位时出现异常", excute_rs.getErr());
						logger.error("关闭" + pair + ps.getMemo() + "仓位时出现异常", excute_rs.getErr());
					}
				}
			}
		} catch (Exception e) {
			logger.error("自动关闭" + pair + "仓位时出现异常", e);
		} finally {
			try {
				if(this.websocketApi != null) {
					this.websocketApi.close();
				}
			}catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
