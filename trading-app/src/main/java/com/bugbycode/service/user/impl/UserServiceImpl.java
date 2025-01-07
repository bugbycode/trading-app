package com.bugbycode.service.user.impl;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bugbycode.module.MonitorStatus;
import com.bugbycode.module.RecvCrossUnPnlStatus;
import com.bugbycode.module.Regex;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.DrawTrade;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.service.user.UserService;
import com.util.RegexUtil;
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
		if(!RegexUtil.test(username, Regex.EMAIL)) {
			throw new UsernameNotFoundException("用户名密码错误");
		}
		User user = userRepository.queryByUsername(username);
		if(user == null) {
			throw new UsernameNotFoundException("用户名密码错误");
		}
		if(StringUtil.isNotEmpty(user.getPassword())) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		return user;
	}

	public String getSubscribeAiUserEmail(List <User> userList) {
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
		List <User> userList = userRepository.queryAllUserByFibMonitor(MonitorStatus.OPEN);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getRiseAndFallMonitorUserEmail() {
		List <User> userList = userRepository.queryAllUserByRiseAndFallMonitor(MonitorStatus.OPEN);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getEmaMonitorUserEmail() {
		List <User> userList = userRepository.queryAllUserByEmaMonitor(MonitorStatus.OPEN);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getEmaRiseAndFallUserEmail() {
		List <User> userList = userRepository.queryAllUserByEmaRiseAndFall(MonitorStatus.OPEN);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getHighOrLowMonitorUserEmail() {
		List <User> userList = userRepository.queryAllUserByHighOrLowMonitor(MonitorStatus.OPEN);
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public String getAreaMonitorUserEmail() {
		List <User> userList = userRepository.queryAllUserByAreaMonitor(MonitorStatus.OPEN);
		return getSubscribeAiUserEmail(userList);
	}
	
	@Override
	public String getAllUserEmail() {
		List <User> userList = userRepository.queryAllUser();
		return getSubscribeAiUserEmail(userList);
	}

	@Override
	public List<User> queryByAutoTrade(AutoTrade autoTrade,AutoTradeType autoTradeType) {
		return userRepository.queryByAutoTrade(autoTrade,autoTradeType);
	}

	@Override
	public List<User> queryByDrawTrade(DrawTrade drawTrade) {
		return userRepository.queryByDrawTrade(drawTrade);
	}

	@Override
	public List<User> queryByRecvCrossUnPnl(RecvCrossUnPnlStatus status) {
		return userRepository.queryByRecvCrossUnPnl(status);
	}
}
