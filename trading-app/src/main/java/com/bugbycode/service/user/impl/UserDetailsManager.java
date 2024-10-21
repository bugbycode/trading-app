package com.bugbycode.service.user.impl;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.util.StringUtil;

public class UserDetailsManager implements UserDetailsService {

	private final UserRepository userRepository;
	
	private final PasswordEncoder passwordEncoder;
	
	public UserDetailsManager(UserRepository userRepository,PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.queryByUsername(username);
		if(user == null) {
			throw new UsernameNotFoundException("user not fond.");
		}
		if(StringUtil.isNotEmpty(user.getPassword())) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		return user;
	}

}
