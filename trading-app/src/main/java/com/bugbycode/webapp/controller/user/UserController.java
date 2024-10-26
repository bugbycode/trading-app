package com.bugbycode.webapp.controller.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugbycode.module.ResultCode;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.webapp.controller.base.BaseController;
import com.util.MD5Util;
import com.util.StringUtil;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController{

	private final Logger logger = LogManager.getLogger(UserController.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/userInfo")
	public User userInfo() {
		return getUserInfo();
	}
	
	@PostMapping("/changPwd")
	public String changPwd(String oldPwd,String newPwd) {
		
		ResultCode code = ResultCode.ERROR;
		
		JSONObject json = new JSONObject();
		User user = getUserInfo();
		User dbUser = userRepository.queryByUsername(user.getUsername());
		if(dbUser == null) {
			throw new AccessDeniedException("无权访问");
		}
		
		if(StringUtil.isEmpty(oldPwd)) {
			json.put("message", "请输入旧密码");
		} else if(StringUtil.isEmpty(newPwd)) {
			json.put("message", "请输入新密码");
		} else if(!MD5Util.md5(oldPwd).equals(dbUser.getPassword())) {
			json.put("message", "旧密码错误");
		} else {
			dbUser.setPassword(MD5Util.md5(newPwd));
			userRepository.update(dbUser);
			code = ResultCode.SUCCESS;
			json.put("message", "修改密码成功");
		}
		
		json.put("code", code.getCode());
		
		return json.toString();
	}
}
