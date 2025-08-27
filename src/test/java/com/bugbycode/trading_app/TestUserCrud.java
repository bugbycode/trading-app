package com.bugbycode.trading_app;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.util.MD5Util;

import jakarta.annotation.Resource;
import net.minidev.json.JSONArray;

@SpringBootTest
public class TestUserCrud {

    private final Logger logger = LogManager.getLogger(TestUserCrud.class);

    @Resource
    private UserRepository userRepository;

    @Test
    public void testInsert(){
        User user = new User();
        user.setUsername("test@gmail.com");
        user.setPassword(MD5Util.md5("123456"));
        
        User dbUser = userRepository.queryByUsername(user.getUsername());
        if(dbUser != null){
            logger.info("用户已存在");
            return;
        }

        userRepository.insert(user);
    }

    @Test
    public void testQueryAutoTradeUser(){
        List<User> userList = userRepository.queryByAutoTrade(AutoTrade.OPEN, AutoTradeType.FIB_RET);
        logger.info(new org.json.JSONArray(userList));
    }

    @Test
    public void testQuery() {
        User u = userRepository.queryByUsername("test@gmail.com");
        logger.info(u.getTradeNumber());
    }
}
