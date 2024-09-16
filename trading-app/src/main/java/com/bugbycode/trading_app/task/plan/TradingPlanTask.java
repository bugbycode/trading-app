package com.bugbycode.trading_app.task.plan;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.TradingPlan;
import com.bugbycode.repository.plan.PlanRepository;
import com.bugbycode.service.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.plan.work.SyncKlinesTask;
import com.util.PlanPairSet;

/**
 * 交易计划任务
 */
@Configuration
@EnableScheduling
public class TradingPlanTask {

	private final Logger logger = LogManager.getLogger(TradingPlanTask.class);
	
	@Autowired
	private WorkTaskPool analysisWorkTaskPool;
	
    @Autowired
    private WorkTaskPool workTaskPool;
	
	@Autowired
	private PlanRepository planRepository;
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 交易计划定时任务 每5分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "6 0/5 * * * ?")
	public void execute() throws Exception {
		
		Date now = new Date();
		
		//查询所有任务
		List<TradingPlan> list = planRepository.findAll();
		
		if(CollectionUtils.isEmpty(list)) {
			logger.info("当前没有交易计划可执行");
			return;
		}
		
		PlanPairSet planPairSet = new PlanPairSet(); 
		planPairSet.addPair(list);
		
		for(String pair : planPairSet) {
			workTaskPool.add(new SyncKlinesTask(klinesService, planRepository, analysisWorkTaskPool, pair, now));
		}
	}
}
