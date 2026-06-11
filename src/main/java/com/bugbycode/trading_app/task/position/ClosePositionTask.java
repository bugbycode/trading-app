package com.bugbycode.trading_app.task.position;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import com.bugbycode.binance.module.position.PositionInfo;
import com.bugbycode.binance.trade.rest.BinanceRestTradeService;
import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.exception.OrderPlaceException;
import com.bugbycode.module.Result;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.email.SendMailTask;
import com.util.DateFormatUtil;

/**
 * 自动关闭现有持仓
 */
@Configuration
@EnableScheduling
public class ClosePositionTask {

	private final Logger logger = LogManager.getLogger(ClosePositionTask.class);
	
	@Autowired
	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	@Autowired
    private UserRepository userRepository;

	@Autowired
    private BinanceRestTradeService binanceRestTradeService;
	
	@Autowired
	private WorkTaskPool emailWorkTaskPool;
	
	//每天上午八点整执行
	@Scheduled(cron = "0 0 8 * * ?")
	public void executeTask() {
		
		if(AppConfig.DEBUG) {
			return;
		}
		
		//查询所有启用墙头草策略用户
		List<User> userList = userRepository.queryByAutoTrade(AutoTrade.OPEN, AutoTradeType.FENCE_SITTER);
		if(!CollectionUtils.isEmpty(userList)) {
			for(User u : userList) {
				try {
					String tradeUserEmail = u.getUsername();
					String dateStr = DateFormatUtil.format(new Date());
					String binanceApiKey = u.getBinanceApiKey();
					String binanceSecretKey = u.getBinanceSecretKey();
					List<PositionInfo> positionList = binanceRestTradeService.positionRisk_v3(binanceApiKey, binanceSecretKey, null);
					for(PositionInfo info : positionList) {
						Result<BinanceOrderInfo, RuntimeException> rs = binanceWebsocketTradeService.closePositionInfo(binanceApiKey, binanceSecretKey, info);
						RuntimeException ex = rs.getErr();
						if(ex == null) {
							logger.info("{}仓位已平仓", info.getSymbol());
						} else {
							String title = "平仓" + info.getSymbol() + "仓位时出现异常";
							String message = ex.getMessage();
							if(ex instanceof OrderPlaceException) {
								title = ((OrderPlaceException)ex).getTitle();
							}
							emailWorkTaskPool.add(new SendMailTask(u, title + " " + dateStr, message, tradeUserEmail, userRepository));
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
}
