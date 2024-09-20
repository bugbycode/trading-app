package com.bugbycode.trading_app.task.sync;

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
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.sync.work.SyncKlinesTask;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.KlinesUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 从币安同步k线定时任务
 */
@Configuration
@EnableScheduling
public class FromBinancePullKlinesTask {

    private final Logger logger = LogManager.getLogger(FromBinancePullKlinesTask.class);

    @Autowired
	private KlinesService klinesService;

    @Autowired
    private KlinesRepository klinesRepository;

    @Autowired
    private WorkTaskPool workTaskPool;

	@Autowired
	private WorkTaskPool analysisWorkTaskPool;

    /**
	 * 查询k线信息
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "5 0 * * * ?")
	public void continuousKlines() throws Exception {
		
		logger.info("FromBinancePullKlinesTask start.");
		
		Date now = new Date();
        
		try {
			
			for(String pair : AppConfig.PAIRS) {

				pair = pair.trim();
				
                if(StringUtil.isEmpty(pair)) {
					continue;
				}
                
                workTaskPool.add(new SyncKlinesTask(pair, now, klinesService, klinesRepository,analysisWorkTaskPool));
            }
        } catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		} finally {
			logger.info("FromBinancePullKlinesTask finish.");
		}
    }
}
