package com.bugbycode.webapp.controller.bot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugbycode.repository.trading.OrderRepository;
import com.bugbycode.webapp.controller.base.BaseController;
import com.util.PriceUtil;

@RestController
@RequestMapping("/bot")
public class BotController extends BaseController{

	@Autowired
	private OrderRepository orderRepository;
	
	@GetMapping("/getOrderCount")
	public Map<String,String> getOrderCount(){
		long total = orderRepository.countAll(null, 1);
		long countPnl = orderRepository.countPositivePnlOrders();
		double sumPnl = orderRepository.sumPnl();
		Map<String,String> result = new HashMap<String,String>();
		
		double win = total == 0 ? 0 : (countPnl * 1.0) / total * 100;
		
		result.put("winning", PriceUtil.formatDoubleDecimal(win, 2) + "%");
		result.put("pnl", PriceUtil.formatDoubleDecimal(sumPnl,2));
		return result;
	}
}
