package com.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.ResultCode;

public class ProcessUtil {
	
	private final static Logger logger = LogManager.getLogger(ProcessUtil.class);

	public static long getMongoDbPid() {
		
		long pId = -1;
		
		String cmd = "ps -ef | grep mongod | grep -v grep | awk '{print $2}'";
		
		try {
			
			logger.info("run \"{}\"", cmd);
			
			String pidStr = CommandUtil.run(cmd);
			
			pidStr = pidStr.trim();
			
			logger.info(pidStr);
			
			if(StringUtil.isNotEmpty(pidStr)) {
				pId = Long.valueOf(pidStr);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return pId;
	}
	
	public static synchronized ResultCode startupMongod() {
		
		ResultCode code = ResultCode.ERROR;

		String cmd = "/usr/local/mongodb/bin/start.sh";
		
		try {

			long pId = getMongoDbPid();
			
			if(pId == -1) {
				logger.info("run \"{}\"", cmd);
				String result = CommandUtil.run(cmd);
				logger.info(result);
			}
			
			pId = getMongoDbPid();
			
			logger.info("Mongodb pid as {}", pId);
			
			if(pId > 0) {
				code = ResultCode.SUCCESS;
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return code;
	}
}
