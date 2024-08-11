package com.bugbycode.trading_app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AppJunitTest {

	private final Logger logger = LogManager.getLogger(AppJunitTest.class);

	
	
	@Test
	public void testMain() throws Exception {
		
	}
	
}
