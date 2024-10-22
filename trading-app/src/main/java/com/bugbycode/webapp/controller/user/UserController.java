package com.bugbycode.webapp.controller.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugbycode.module.user.User;
import com.bugbycode.webapp.controller.base.BaseController;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController{

	private final Logger logger = LogManager.getLogger(UserController.class);
	
	@GetMapping("/userInfo")
	public User userInfo() {
		return getUserInfo();
	}
}
