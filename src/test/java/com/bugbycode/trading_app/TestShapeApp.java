package com.bugbycode.trading_app;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.module.Klines;
import com.bugbycode.module.ShapeInfo;
import com.bugbycode.module.shape.ShapeType;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.repository.shape.ShapeRepository;
import com.bugbycode.service.shape.ShapeService;
import com.bugbycode.trading_app.task.email.SendMailTask;
import com.util.DateFormatUtil;
import com.util.PriceUtil;
import com.util.StraightLineUtil;
import com.util.page.Page;
import com.util.page.SearchResult;

@SpringBootTest
public class TestShapeApp {

    private final Logger logger = LogManager.getLogger(TestShapeApp.class);

    @Autowired
    private ShapeRepository shapeRepository;

	@Autowired
	private ShapeService shapeService;

	@Autowired
	private KlinesRepository klinesRepository;

    @Test
    public void testCalc() throws ParseException{
        String pair = "BTCUSDT";
        ShapeInfo info = shapeRepository.queryBySymbol(pair).get(0);
        JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		if(pointsJsonArray.length() > 1) {
			JSONObject points = pointsJsonArray.getJSONObject(0);
			double price0 = points.getDouble("price");
			long time0 = points.getLong("time") * 1000;
			
			JSONObject points1 = pointsJsonArray.getJSONObject(1);
			double price1 = points1.getDouble("price");
			long time1 = points1.getLong("time") * 1000;
			
			StraightLineUtil util = new StraightLineUtil(time0, price0, time1, price1);
			double resultPrice = util.calculateLineYvalue(DateFormatUtil.parse("2024-10-23 23:05:00").getTime());
			logger.info(resultPrice);
			
		}
    }

	@Test
	public void testQuery(){
		String owner = "bugbycode@gmail.com";
		String symbol = "";
		
		int limit = 10;
		long currentPage = 2;
		long skip = currentPage * limit - limit;

		SearchResult<ShapeInfo> sr = shapeService.query(owner, symbol, skip, limit);
		logger.info(new JSONObject(sr));
		logger.info("totalCount:" + sr.getPage().getTotalCount());
		logger.info("pageTotal:" + sr.getList().size());
	}

	@Test
	public void testShapeInfo(){
		String pair = "DOGEUSDT";
		List<ShapeInfo> shapeList = shapeRepository.queryBySymbol(pair);
		for(ShapeInfo info : shapeList) {
			if(info.getShapeType() != ShapeType.LINE_TOOL_FIXED_RANGE_VOLUME_PROFILE) {
				continue;
			}
			//价格坐标
			JSONArray pointsJsonArray = new JSONArray(info.getPoints());
			JSONObject points = pointsJsonArray.getJSONObject(0);
			//double price0 = points.getDouble("price");
			long time0 = points.getLong("time") * 1000;
			
			JSONObject points1 = pointsJsonArray.getJSONObject(1);
			//double price1 = points1.getDouble("price");
			long time1 = points1.getLong("time") * 1000;
			List<Klines> list = klinesRepository.findByTimeLimit(info.getSymbol(), info.getInervalType(), time0, time1);
			logger.info(list);
			Klines high = PriceUtil.getMaxPriceKLine(list);
			Klines low = PriceUtil.getMinPriceKLine(list);
			Klines highBody = PriceUtil.getMaxBodyHighPriceKLine(list);
			Klines lowBody = PriceUtil.getMinBodyHighPriceKLine(list);
			double highPrice = high.getHighPriceDoubleValue();
			double lowPrice = low.getLowPriceDoubleValue();
			double highBodyPrice = highBody.getBodyHighPriceDoubleValue();
			double lowBodyPrice = lowBody.getBodyLowPriceDoubleValue();
			
			logger.info("{}永续合约盘整区最高价格：{}，最低价格：{}，顶部价格：{}，底部价格：{}", pair, highPrice, lowPrice, highBodyPrice, lowBodyPrice);
		}
	}
}
