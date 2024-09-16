package com.bugbycode.trading_app.task.plan.work;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.Klines;
import com.bugbycode.module.LongOrShortType;
import com.bugbycode.module.PlanStatus;
import com.bugbycode.module.TradingPlan;
import com.bugbycode.repository.plan.PlanRepository;
import com.bugbycode.service.klines.KlinesService;
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
	
	private PlanRepository planRepository;
	
	public AnalysisKlinesTask(TradingPlan plan, Klines hitKlines,KlinesService klinesService,
			PlanRepository planRepository) {
		this.plan = plan;
		this.hitKlines = hitKlines;
		this.klinesService = klinesService;
		this.planRepository = planRepository;
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
			String text = StringUtil.formatPlan(plan, hitKlines.getDecimalNum());
			
			switch (type) {
			case LONG: {
				//最低价小于条件价 开盘价大于条件价
				if(hitKlines.getLowPrice() <= hitPrice && hitKlines.getOpenPrice() >= hitPrice) {
					subject = plan.getPair() + "永续合约(" + plan.getHitPrice() + ")做多交易计划 " + dateStr;
				}
				break;
			}
			default:
				//最高价大于条件价 开盘价小于条件价
				if(hitKlines.getHighPrice() >= hitPrice && hitKlines.getOpenPrice() <= hitPrice) {
					subject = plan.getPair() + "永续合约(" + plan.getHitPrice() + ")做空交易计划 " + dateStr;
				}
			}
			
			if(StringUtil.isNotEmpty(subject)) {
				klinesService.sendEmail(subject, text, null);
				planRepository.deleteById(plan.getId());
				logger.info("交易计划任务已触发，已从数据库中删除");
			}
			
		} catch (Exception e) {
			logger.error("执行交易计划分析任务时出现异常", e);
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		}
	}

}
