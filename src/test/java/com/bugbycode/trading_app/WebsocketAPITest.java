package com.bugbycode.trading_app;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.binance.trade.rest.BinanceRestTradeService;
import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.exception.OrderCancelException;
import com.bugbycode.exception.OrderPlaceException;
import com.bugbycode.module.PlaceOrderAgain;
import com.bugbycode.module.ResultCode;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.CallbackRateEnabled;
import com.bugbycode.module.binance.Leverage;
import com.bugbycode.module.binance.MarginType;
import com.bugbycode.module.binance.PriceInfo;
import com.bugbycode.module.binance.ProfitOrderEnabled;
import com.bugbycode.module.binance.Result;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.module.binance.WorkingType;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.module.trading.Type;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.util.PriceUtil;

@SpringBootTest
public class WebsocketAPITest {

	private final Logger logger = LogManager.getLogger(WebsocketAPITest.class);
	
	private String binanceApiKey;
	
	private String binanceSecretKey;
	
	@Autowired
	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	@Autowired
    private UserRepository userRepository;

	@Autowired
    private BinanceRestTradeService binanceRestTradeService;

    @Autowired
    private BinanceExchangeService binanceExchangeService;
	
	private User user;
	
	@BeforeEach
	public void befor() {
		
		System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", "50000");
        user = userRepository.queryByUsername("bugbycode@gmail.com");
        binanceExchangeService.exchangeInfo();
        
        binanceApiKey = user.getBinanceApiKey();
        binanceSecretKey = user.getBinanceSecretKey();
	}
	
	@Test
	public void testBalance(){
		List<Balance> balanceList2 = binanceWebsocketTradeService.balance_v2(binanceApiKey, binanceSecretKey);
		logger.info(new JSONArray(balanceList2));
	}

	@Test
	public void testGetPrice(){
		binanceWebsocketTradeService.getPrice("BTCUSDT");
		long t = new Date().getTime();
		PriceInfo info = binanceWebsocketTradeService.getPrice("BTCUSDT");
		logger.info(info.getPrice());
		logger.info(new Date().getTime() - t);
	}

	@Test
    public void testOrderPost(){
        String symbol = "USDCUSDT";
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
        BinanceOrderInfo order = binanceWebsocketTradeService.order_place(binanceApiKey, binanceSecretKey,
        symbol, Side.SELL, PositionSide.SHORT, Type.MARKET, 
        null, new BigDecimal("6"), null, 
        null, null, null, null, null, PlaceOrderAgain.CLOSE);

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
        BinanceOrderInfo stop_order = binanceWebsocketTradeService.order_place(binanceApiKey, binanceSecretKey,
        symbol, Side.BUY, PositionSide.SHORT, Type.STOP_MARKET, 
        null, new BigDecimal(order.getOrigQty()), null, 
        new BigDecimal("0.9998"), true, WorkingType.CONTRACT_PRICE, null, null, PlaceOrderAgain.CLOSE);

        if(stop_order.getOrderId() > 0) {
            logger.info("市价止损下单成功");
        }

        //市价止盈单测试
        BinanceOrderInfo take_order = binanceWebsocketTradeService.order_place(binanceApiKey, binanceSecretKey,
        symbol, Side.BUY, PositionSide.SHORT, Type.TAKE_PROFIT_MARKET, 
        null, new BigDecimal(order.getOrigQty()), null, 
        new BigDecimal("0.998"), true, WorkingType.CONTRACT_PRICE, null, null, PlaceOrderAgain.CLOSE);
        if(take_order.getOrderId() > 0) {
            logger.info("市价止盈下单成功");
        }

    }

	@Test
	public void testAvailableBalance() {
		String availableBalance = binanceWebsocketTradeService.availableBalance(binanceApiKey, binanceSecretKey, "USDT");
		logger.info(availableBalance);
	}

	@Test
	public void testTradeMarket(){
		String symbol = "USDCUSDT";
		PositionSide ps = PositionSide.LONG;
		BigDecimal quantity = new BigDecimal(String.valueOf(6));
		BigDecimal stopLoss = new BigDecimal(String.valueOf(0.9994));
		BigDecimal takeProfit = new BigDecimal(String.valueOf(1));
		BigDecimal callbackRate = new BigDecimal("0.5");
		BigDecimal activationPrice = new BigDecimal("0.9998");
		ProfitOrderEnabled profitOrderEnabled = ProfitOrderEnabled.OPEN;
		try {
			binanceWebsocketTradeService.tradeMarket(binanceApiKey, binanceSecretKey, symbol, ps,
					quantity, stopLoss, takeProfit, CallbackRateEnabled.OPEN, activationPrice, callbackRate, profitOrderEnabled);
		} catch (Exception e) {
			if(e instanceof OrderPlaceException) {
				logger.error(((OrderPlaceException)e).getTitle(), e);
			} else {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	@Test
	public void testOrderCancel() {
		long orderId = 2227229613l;
		String symbol = "USDCUSDT";
		try {
			binanceWebsocketTradeService.orderCancel(binanceApiKey, binanceSecretKey, symbol, orderId);
		} catch (Exception e) {
			if(e instanceof OrderCancelException) {
				logger.error(((OrderCancelException)e).getTitle(), e);
			} else {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	@Test
	public void testTradingStopMarket() {
		String symbol = "OGUSDT";
		PositionSide ps = PositionSide.LONG;
		Side side = Side.SELL;
		BigDecimal activationPrice = new BigDecimal("14");
		BigDecimal callbackRate = new BigDecimal("3");
        BigDecimal quantity = new BigDecimal("0.1");
		binanceWebsocketTradeService.order_place(binanceApiKey, binanceSecretKey, symbol, side, ps, Type.TRAILING_STOP_MARKET, 
				null, quantity, null, null, true, WorkingType.CONTRACT_PRICE, activationPrice, callbackRate, PlaceOrderAgain.CLOSE);
	}

    @Test
    public void testBinanceExchange() {
        String symbol = "BTCUSDT";
        
        logger.info(new BigDecimal(binanceWebsocketTradeService.getMarketMinQuantity(symbol)).multiply(new BigDecimal(2)));
    }

    @Test
    public void testPrice(){
        PriceInfo priceInfo = binanceWebsocketTradeService.getPrice("币安人生USDT");
        logger.info(priceInfo.getPrice());
        int decimalNum = new BigDecimal(String.valueOf(Double.valueOf(priceInfo.getPrice()))).scale();
        logger.info(decimalNum);
    }
    
    @Test
    public void testRectificationQuantity() {
    	String pair = "SOLUSDT";
    	int baseStepSize = 2;
    	double positionValue = 400;
    	
    	PriceInfo priceInfo = binanceWebsocketTradeService.getPrice(pair);
    	//最少下单数量
		String quantityNum = binanceWebsocketTradeService.getMarketMinQuantity(pair);
		BigDecimal minQuantity = new BigDecimal(quantityNum);
		
		//持仓数量 = 最小持仓数量 x 名义价值倍数
		BigDecimal quantity = new BigDecimal(quantityNum);
		quantity = quantity.multiply(new BigDecimal(baseStepSize));
		
		BigDecimal newQuantity = PriceUtil.rectificationQuantity(quantity, minQuantity, baseStepSize, positionValue, priceInfo);
		logger.info("交易对：{}，名义价值倍数：{}，持仓价值限额：{}，最小持仓数量：{}，预期持仓数量：{}，新计算持仓数量：{}，新计算持仓价值：{}",
				pair,baseStepSize,positionValue, minQuantity, quantity, newQuantity, newQuantity.multiply(priceInfo.getPriceBigDecimalValue()));
    }

    @Test
    public void testTakeProfit(){
        PriceInfo priceInfo = binanceWebsocketTradeService.getPrice("SPELLUSDT");
        double takePrice = PriceUtil.getShortTakeProfitForPercent(priceInfo.getPriceDoubleValue(), 0.4);
        logger.info(takePrice);
    }
    
    @Test
    public void testGetMarketMinQuantity() {
    	String pair = "BTCUSDT";
    	//最少下单数量
		String quantityNum = binanceWebsocketTradeService.getMarketMinQuantity(pair);
		logger.info(quantityNum);
    }
    

	@AfterEach
	public void after(){
		
	}
}
