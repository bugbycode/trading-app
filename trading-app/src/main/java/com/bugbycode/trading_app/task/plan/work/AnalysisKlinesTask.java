package com.bugbycode.trading_app.task.plan.work;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.Klines;
import com.bugbycode.module.LongOrShortType;
import com.bugbycode.module.PlanStatus;
import com.bugbycode.module.TradingPlan;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.service.plan.TradingPlanService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.StringUtil;

/**
 * 交易计划分析任务
 */
public class AnalysisKlinesTask implements Runnable {

	private final Logger logger = LogManager.getLogger(AnalysisKlinesTask.class);
	
	private TradingPlan plan;
	
	private Klines hitKlines;
	
	private KlinesService klinesService;
	
	private TradingPlanService tradingPlanService;
	
	public AnalysisKlinesTask(TradingPlan plan, Klines hitKlines,KlinesService klinesService,
			TradingPlanService tradingPlanService) {
		this.plan = plan;
		this.hitKlines = hitKlines;
		this.klinesService = klinesService;
		this.tradingPlanService = tradingPlanService;
	}

	@Override
	public void run() {
		String dateStr = DateFormatUtil.format(new Date());
		try {
			if(plan.getPlanStatus() == PlanStatus.IN_VALID) {
				logger.info("任务已失效");
				return;
			}
			
			LongOrShortType type = plan.getLongOrShortType();
			double hitPrice = plan.getHitPrice();
			
			String subject = "";
			//String text = StringUtil.formatPlan(plan, hitKlines.getDecimalNum());
			String text = "";
			
			switch (type) {
			case LONG: {
				if(hitKlines.getLowPrice() <= hitPrice && hitKlines.getHighPrice() >= hitPrice) {
					subject = plan.getPair() + "永续合约(" + plan.getHitPrice() + ")做多交易计划 " + dateStr;
					text = plan.getPair() + "永续合约价格已下跌到(" + plan.getHitPrice() + ")，请注意查看是否存在做多机会！";
				}
				break;
			}
			default:
				if(hitKlines.getLowPrice() <= hitPrice && hitKlines.getHighPrice() >= hitPrice) {
					subject = plan.getPair() + "永续合约(" + plan.getHitPrice() + ")做空交易计划 " + dateStr;
					text = plan.getPair() + "永续合约价格已上涨到(" + plan.getHitPrice() + ")，请注意查看是否存在做空机会！";
				}
			}
			
			if(StringUtil.isNotEmpty(subject)) {
				klinesService.sendEmail(subject, text, null);
				tradingPlanService.removeTradingPlan(plan.getFilename());
				logger.info("交易计划任务已触发，已将其文件(" + plan.getFilename() + ")信息删除");
			}
			
		} catch (Exception e) {
			logger.error("执行交易计划分析任务时出现异常", e);
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		}
	}

}
