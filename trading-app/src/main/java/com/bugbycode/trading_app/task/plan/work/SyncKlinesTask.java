package com.bugbycode.trading_app.task.plan.work;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Klines;
import com.bugbycode.module.PlanStatus;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.module.TradingPlan;
import com.bugbycode.repository.plan.PlanRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.util.EmailUtil;
import com.util.PriceUtil;

/**
 * 交易计划同步k线任务
 */
public class SyncKlinesTask implements Runnable{
	
	private final Logger logger = LogManager.getLogger(SyncKlinesTask.class);
	
	private KlinesService klinesService;
	
	private PlanRepository planRepository;
	
	private WorkTaskPool analysisWorkTaskPool;
	
	private String pair;
	
	private Date now;
	
	public SyncKlinesTask(KlinesService klinesService, PlanRepository planRepository, WorkTaskPool analysisWorkTaskPool, String pair, Date now) {
		this.klinesService = klinesService;
		this.planRepository = planRepository;
		this.pair = pair;
		this.now = now;
		this.analysisWorkTaskPool = analysisWorkTaskPool;
	}

	@Override
	public void run() {
		try {
			List<TradingPlan> list = planRepository.find(pair);
			if(!CollectionUtils.isEmpty(list)) {
				for(TradingPlan plan : list) {
					if(plan.getPlanStatus() == PlanStatus.IN_VALID) {
						logger.info("任务已失效");
						return;
					}
					//查询近5分钟k线信息
					List<Klines> list_1_x_15m = klinesService.continuousKlines5M(plan.getPair(), now, 1, QUERY_SPLIT.NOT_ENDTIME);
					if(CollectionUtils.isEmpty(list_1_x_15m)) {
						logger.info("无法获取交易对" + plan.getPair() + "近5分钟k线信息");
						return;
					}
					
					Klines klines = PriceUtil.getLastKlines(list_1_x_15m);
					
					//logger.info(klines);
					
					analysisWorkTaskPool.add(new AnalysisKlinesTask(plan, klines, klinesService, planRepository));
				}
			}
		} catch (Exception e) {
			logger.error("执行交易计划同步k线任务时出现异常", e);
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		}
	}

}
