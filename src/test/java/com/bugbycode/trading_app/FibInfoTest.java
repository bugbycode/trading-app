package com.bugbycode.trading_app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;

@SpringBootTest
public class FibInfoTest {

	private final Logger logger = LogManager.getLogger(FibInfoTest.class);
	
	@Test
	public void testGetActivationPriceCode() {
		FibInfo fibInfo = new FibInfo(1, 100, 2, FibLevel.LEVEL_0);
		FibCode[] codes = FibCode.values();
		for(FibCode code : codes) {
			logger.info("{} - {}", code, fibInfo.getActivationPriceCode(code));
		}
	}
}
