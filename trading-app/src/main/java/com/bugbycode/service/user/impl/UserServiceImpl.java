package com.bugbycode.service.user.impl;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.service.user.UserService;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@Service("userDetailsService")
public class UserServiceImpl implements UserService{

	@Resource
	private UserRepository userRepository;

	@Resource
	private PasswordEncoder passwordEncoder;
	
	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.queryByUsername(username);
		if(user == null) {
			throw new UsernameNotFoundException("用户不存在");
		}
		if(StringUtil.isNotEmpty(user.getPassword())) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		return user;
	}

	private String getSubscribeAiUserEmail(List <User> userList) {
		StringBuffer buff = new StringBuffer();
		for(User user : userList) {
			if(buff.length() > 0) {
				buff.append(",");
			}
			buff.append(user.getUsername());
		}
		return buff.toString();
	}

	@Override
	public String getFibMonitorUserEmail() {
		List <User> userList = userRepository.queryAllUserByFibMonitor(1);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getRiseAndFallMonitorUserEmail() {
		List <User> userList = userRepository.queryAllUserByRiseAndFallMonitor(1);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getEmaMonitorUserEmail() {
		List <User> userList = userRepository.queryAllUserByEmaMonitor(1);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getEmaRiseAndFallUserEmail() {
		List <User> userList = userRepository.queryAllUserByEmaRiseAndFall(1);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getHighOrLowMonitorUserEmail() {
		List <User> userList = userRepository.queryAllUserByHighOrLowMonitor(1);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getAllUserEmail() {
		List <User> userList = userRepository.queryAllUser();
		return getSubscribeAiUserEmail(userList);
	}
}
