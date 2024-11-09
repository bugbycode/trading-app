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

	@Override
	public String getSubscribeAiUserEmail() {
		List <User> userList = userRepository.queryAllUserBySubscribeAi(1);
		StringBuffer buff = new StringBuffer();
		for(User user : userList) {
			if(buff.length() > 0) {
				buff.append(",");
			}
			buff.append(user.getUsername());
		}
		return buff.toString();
	}
}
