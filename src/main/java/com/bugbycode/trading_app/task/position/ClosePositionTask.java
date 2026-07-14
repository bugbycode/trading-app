package com.bugbycode.trading_app.task.position;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.binance.module.position.PositionInfo;
import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.binance.trade.websocket.impl.BinanceWebsocketTradeServiceImpl;
import com.bugbycode.config.AppConfig;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.BinanceOrderInfo;
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
	
	/**
	 * 
	 * @param pair 交易对
	 * @param ps 要关闭的持仓方向 LONG/SHORT
	 * @param autoTradeType 指标
	 * @param userDetailsService
	 */
	public ClosePositionTask(String pair, PositionSide ps, AutoTradeType autoTradeType,
			UserService userDetailsService) {
		this.pair = pair;
		this.ps = ps;
		this.autoTradeType = autoTradeType;
		this.userDetailsService = userDetailsService;
	}

	@Override
	public void run() {
		try {
			this.websocketApi = new TradingWebSocketClientEndpoint(AppConfig.WEBSOCKET_API_URL);
			this.binanceWebsocketTradeService = new BinanceWebsocketTradeServiceImpl(this.websocketApi);
			List<User> userList = userDetailsService.queryByAutoTrade(AutoTrade.OPEN, autoTradeType);
			for(User u : userList) {
				String binanceApiKey = u.getBinanceApiKey();
				String binanceSecretKey = u.getBinanceSecretKey();
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
