package com.bugbycode.trading_app.task.operations;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.open_interest.OpenInterestHist;
import com.bugbycode.repository.collections.CollectionsRepository;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;

/**
 * 检查已下架交易对任务
 */
@Configuration
@EnableScheduling
public class CheckExpiredDataTask {

	private final Logger logger = LogManager.getLogger(CheckExpiredDataTask.class);
	
	@Autowired
	private OpenInterestHistRepository openInterestHistRepository;
	
	@Autowired
	private CollectionsRepository collectionsRepository;
	
	/**
	 * 每五分钟执行一次
	 */
	@Scheduled(cron = "30 3/5 * * * ?")
	public void executeTask() {
		
		if(AppConfig.DEBUG) {
			return;
		}
		
		List<OpenInterestHist> list = openInterestHistRepository.query();
		
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		
		long now = new Date().getTime();
		
		for(OpenInterestHist oih : list) {
			try {
				long t = oih.getTimestamp();
				long d = (now - t) / 1000 / 60 / 60 / 24;
				if(d > 30) {
					String pair = oih.getSymbol();
					logger.info("{}交易对已超过{}天未更新数据", pair, d);
					collectionsRepository.dropCollections(pair, Inerval.INERVAL_1W);
					collectionsRepository.dropCollections(pair, Inerval.INERVAL_1D);
					collectionsRepository.dropCollections(pair, Inerval.INERVAL_4H);
					collectionsRepository.dropCollections(pair, Inerval.INERVAL_1H);
					collectionsRepository.dropCollections(pair, Inerval.INERVAL_15M);
					collectionsRepository.dropCollections(pair, Inerval.INERVAL_5M);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
