package com.bugbycode.trading_app;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.module.ShapeInfo;
import com.bugbycode.repository.shape.ShapeRepository;
import com.bugbycode.service.shape.ShapeService;
import com.bugbycode.trading_app.task.email.SendMailTask;
import com.util.DateFormatUtil;
import com.util.PriceUtil;
import com.util.StraightLineUtil;
import com.util.page.Page;
import com.util.page.SearchResult;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestShapeApp {

    private final Logger logger = LogManager.getLogger(TestShapeApp.class);

    @Autowired
    private ShapeRepository shapeRepository;

	@Autowired
	private ShapeService shapeService;

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
}
