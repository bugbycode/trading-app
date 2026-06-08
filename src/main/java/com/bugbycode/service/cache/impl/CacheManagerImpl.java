package com.bugbycode.service.cache.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bugbycode.config.AppConfig;
import com.bugbycode.service.cache.CacheManager;

@Service("cacheManager")
public class CacheManagerImpl implements CacheManager {

	private final Logger logger = LogManager.getLogger(CacheManagerImpl.class);
	
	@Override
	public void initTradeCahe() {

		try {
			synchronized (AppConfig.LEVERAGE_BRACKET) {
				AppConfig.LEVERAGE_BRACKET.clear();
			}
			synchronized (AppConfig.SYMBOL_CONFIG_INFO) {
				AppConfig.SYMBOL_CONFIG_INFO.clear();
			}
			synchronized (AppConfig.COMMISSION_RATE_CACHE) {
				AppConfig.COMMISSION_RATE_CACHE.clear();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}

}
