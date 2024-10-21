package com.bugbycode.service.user.impl;

import org.springframework.stereotype.Service;

import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.service.user.UserService;

import jakarta.annotation.Resource;

@Service("userService")
public class UserServiceImpl implements UserService{

	@Resource
	private UserRepository userRepository;
	
	@Override
	public String insert(User user) {
		return userRepository.insert(user);
	}

	@Override
	public void update(User user) {
		userRepository.update(user);
	}

	@Override
	public void deleteByUsername(String username) {
		userRepository.deleteByUsername(username);
	}
}
