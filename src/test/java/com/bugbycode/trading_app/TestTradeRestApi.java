package com.bugbycode.trading_app;

import java.math.BigDecimal;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.binance.trade.rest.BinanceRestTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.module.ResultCode;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.Leverage;
import com.bugbycode.module.binance.MarginType;
import com.bugbycode.module.binance.Result;
import com.bugbycode.module.binance.SymbolConfig;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.module.binance.WorkingType;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.module.trading.Type;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class TestTradeRestApi {

    private final Logger logger = LogManager.getLogger(TestTradeRestApi.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BinanceRestTradeService binanceRestTradeService;


    private User user;
    
    private String binanceApiKey;
	
	private String binanceSecretKey;

    @BeforeAll
	public void befor() {
        System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", "50000");
        user = userRepository.queryByUsername("bugbycode@gmail.com");
        binanceApiKey = user.getBinanceApiKey();
        binanceSecretKey = user.getBinanceSecretKey();
    }
    
    @Test
    public void testBalance() {
    	List<Balance> balanceData2 = binanceRestTradeService.balance_v3(binanceApiKey, binanceSecretKey);
    	logger.info(new JSONArray(balanceData2));
    }

    @Test
    public void testDualSidePosition() {
        boolean dualSidePosition = true;
        Result code = binanceRestTradeService.dualSidePosition(user.getBinanceApiKey(), user.getBinanceSecretKey(), dualSidePosition);
        logger.info(code.getMsg());
    }

    @Test
    public void testCheckDualSidePosition(){
        boolean dualSidePosition = binanceRestTradeService.dualSidePosition(user.getBinanceApiKey(), user.getBinanceSecretKey());
        logger.info(dualSidePosition);
    }

    @Test
    public void TestLeverage(){
        String symbol = "BTCUSDT";
        int leverage = 31;
        Leverage lr = binanceRestTradeService.leverage(user.getBinanceApiKey(), user.getBinanceSecretKey(), symbol, leverage);
        logger.info(lr.getLeverage());
    }
    
    @Test
    public void testMarginType() {
    	String symbol = "BTCUSDT";
    	Result result = binanceRestTradeService.marginType(user.getBinanceApiKey(), user.getBinanceSecretKey(), symbol, MarginType.CROSSED);
    	logger.info(result.getResult());
    }

    @Test
    public void testOpenOrders() {
    	String symbol = "BTCUSDT";
    	List<BinanceOrderInfo> orders = binanceRestTradeService.openOrders(user.getBinanceApiKey(), user.getBinanceSecretKey(), symbol);
        logger.info(new JSONArray(orders));
    }

    @Test
    public void testOpenOrder() {
    	String symbol = "GLMUSDT";
    	BinanceOrderInfo order = binanceRestTradeService.openOrder(user.getBinanceApiKey(), user.getBinanceSecretKey(), symbol,0,"ios_RmL9vjtV72Cegy3w5MbR");
        logger.info(new JSONObject(order));
    }
    
    @Test
    public void testAllOrders() {
    	String symbol = "GLMUSDT";
    	List<BinanceOrderInfo> orders = binanceRestTradeService.allOrders(user.getBinanceApiKey(), user.getBinanceSecretKey(), symbol,0,0,0,0);
        logger.info(new JSONArray(orders));
    }

    @Test
    public void testOrders() {
    	String symbol = "USDCUSDT";
    	BinanceOrderInfo order = binanceRestTradeService.order(user.getBinanceApiKey(), user.getBinanceSecretKey(), symbol,1074704444,"");
        logger.info(new JSONObject(order));
    }

    @Test
    public void testOrderPost(){
        String symbol = "ADAUSDT";
        //调整保证金模式为逐仓
        Result result = binanceRestTradeService.marginType(binanceApiKey, binanceSecretKey, symbol, MarginType.ISOLATED);
        if(result.getResult() == ResultCode.SUCCESS) {
            logger.info("修改保证金模式成功");
        } else {
            logger.info("修改保证金模式失败");
        }

        Leverage leverage = binanceRestTradeService.leverage(binanceApiKey, binanceSecretKey, symbol,50);
        if(leverage.getLeverage() == 50){
            logger.info("修改杠杆倍数成功");
        }

        //限价单测试
        /*
        BinanceOrderInfo order = binanceRestTradeService.orderPost(binanceApiKey, binanceSecretKey, 
        symbol, Side.BUY, PositionSide.LONG, 
        Type.LIMIT, null, new BigDecimal("10.0"), 
        new BigDecimal("0.99"), null, null, null); */
        //市价单测试
        BinanceOrderInfo order = binanceRestTradeService.orderPost(binanceApiKey, binanceSecretKey, 
        symbol, Side.SELL, PositionSide.SHORT, Type.MARKET, 
        null, new BigDecimal("6"), null, 
        null, null, null);

        long orderId = order.getOrderId();

        //logger.info(new JSONObject(order));
        if(order.getOrderId() > 0) {
            logger.info("市价下单成功");
        }
        /*
        //限价止损单测试
        BinanceOrderInfo stop_order = binanceRestTradeService.orderPost(binanceApiKey, binanceSecretKey, 
        symbol, Side.SELL, PositionSide.LONG, Type.STOP, 
        null, new BigDecimal(order.getOrigQty()), new BigDecimal("0.998944"), 
        new BigDecimal("0.998944"), null, WorkingType.CONTRACT_PRICE);

        if(stop_order.getOrderId() > 0) {
            logger.info("限价止损下单成功");
        }

        //限价止盈单测试
        BinanceOrderInfo take_order = binanceRestTradeService.orderPost(binanceApiKey, binanceSecretKey, 
        symbol, Side.SELL, PositionSide.LONG, Type.TAKE_PROFIT, 
        null, new BigDecimal(order.getOrigQty()), new BigDecimal("0.998943"), 
        new BigDecimal("0.998943"), null, WorkingType.CONTRACT_PRICE);
        if(take_order.getOrderId() > 0) {
            logger.info("限价止盈下单成功");
        } */

        //市价止损订单测试
        BinanceOrderInfo stop_order = binanceRestTradeService.orderPost(binanceApiKey, binanceSecretKey, 
        symbol, Side.BUY, PositionSide.SHORT, Type.STOP_MARKET, 
        null, new BigDecimal(order.getOrigQty()), null, 
        new BigDecimal("0.9931"), true, WorkingType.CONTRACT_PRICE);

        if(stop_order.getOrderId() > 0) {
            logger.info("市价止损下单成功");
        }

        //市价止盈单测试
        BinanceOrderInfo take_order = binanceRestTradeService.orderPost(binanceApiKey, binanceSecretKey, 
        symbol, Side.BUY, PositionSide.SHORT, Type.TAKE_PROFIT_MARKET, 
        null, new BigDecimal(order.getOrigQty()), null, 
        new BigDecimal("0.932"), true, WorkingType.CONTRACT_PRICE);
        if(take_order.getOrderId() > 0) {
            logger.info("市价止盈下单成功");
        }

    }

    @Test
    public void testDeleteOrder(){
        String symbol = "USDCUSDT";
        long orderId = 1074584285;
        BinanceOrderInfo del_order = binanceRestTradeService.orderDelete(binanceApiKey, binanceSecretKey, 
        symbol, orderId, "");
        if(del_order.getOrderId() > 0){
            logger.info("撤销订单成功");
        }
    }

    @Test
    public void testSymbolConfig(){
        List<SymbolConfig> scList = binanceRestTradeService.getSymbolConfig(binanceApiKey, binanceSecretKey, "BTCUSDT");
        logger.info(new JSONArray(scList));
    }

    @Test
    public void getSymbolConfigBySymbol(){
        String pair = "ETCUSDT";
        SymbolConfig sc = binanceRestTradeService.getSymbolConfigBySymbol(binanceApiKey, binanceSecretKey, pair);
        MarginType marginType = MarginType.resolve(sc.getMarginType());
        if(marginType != MarginType.ISOLATED) {
            binanceRestTradeService.marginType(binanceApiKey, binanceSecretKey, pair, MarginType.ISOLATED);
        }
    }

    @AfterAll
    public void after() {

    }
}
