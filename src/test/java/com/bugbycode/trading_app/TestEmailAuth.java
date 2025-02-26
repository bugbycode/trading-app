package com.bugbycode.trading_app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.module.EmailAuth;
import com.bugbycode.repository.email.EmailRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestEmailAuth {

    private final Logger logger = LogManager.getLogger(TestEmailAuth.class);

    @Autowired
    private EmailRepository emailRepository;

    @Test
    public void testInsertEmailAuth(){
        
    }
}
