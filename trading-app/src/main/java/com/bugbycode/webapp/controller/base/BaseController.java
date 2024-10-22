package com.bugbycode.webapp.controller.base;

import org.springframework.security.core.context.SecurityContextHolder;

import com.bugbycode.module.user.User;

public class BaseController {

	protected User getUserInfo() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		user.setPassword("");
		
		return user;
	}
}
