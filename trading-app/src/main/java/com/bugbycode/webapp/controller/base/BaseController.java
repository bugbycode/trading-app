package com.bugbycode.webapp.controller.base;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bugbycode.module.user.User;

public class BaseController {

	protected User getUserInfo() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if(auth.isAuthenticated()) {
			user = (User)auth.getPrincipal();
			user.setPassword("");
		}
		return user;
	}
}
