package com.bugbycode.trading_app.task.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.module.ResultCode;
import com.util.ProcessUtil;

/**
 * 进程监控任务
 */
@Configuration
@EnableScheduling
public class CheckProcessTask {

	private final Logger logger = LogManager.getLogger(CheckProcessTask.class);
	
	/**
	 * 进程监控任务 每1分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "25 0/1 * * * ?")
	public void executeShapeTask() {

		ResultCode code = ProcessUtil.startupMongod();
		if(code == ResultCode.ERROR) {
			logger.info("Start mongodb error.");
		}
		
	}
	
}
