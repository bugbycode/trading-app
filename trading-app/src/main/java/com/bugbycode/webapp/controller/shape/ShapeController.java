package com.bugbycode.webapp.controller.shape;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.module.ShapeInfo;
import com.bugbycode.module.binance.PriceInfo;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.shape.ShapeRepository;
import com.bugbycode.webapp.controller.base.BaseController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/shape")
public class ShapeController extends BaseController{

	@Resource
	private ShapeRepository shapeRepository;
	
	@Resource
	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	@GetMapping("/getAllShapeInfo")
	public List<ShapeInfo> getAllShapeInfo(){
		User user = getUserInfo();
		return shapeRepository.queryByOwner(user.getUsername());
	}
	
	@PostMapping("/saveShapeInfo")
	public ShapeInfo saveShapeInfo(@RequestBody ShapeInfo info) {
		info.setOwner(getUserInfo().getUsername());
		
		PriceInfo priceInfo = binanceWebsocketTradeService.getPrice(info.getSymbol());
		if(priceInfo != null) {
			info.setPrice(priceInfo.getPrice());
		}
		
		shapeRepository.insert(info);
		return info;
	}
	
	@PostMapping("/updateShapeInfo")
	public ShapeInfo updateShapeInfo(@RequestBody ShapeInfo info) {
		ShapeInfo dbShape = shapeRepository.queryById(info.getId());
		if(dbShape == null || !dbShape.getOwner().equals(getUserInfo().getUsername())) {
			throw new AccessDeniedException("无权访问");
		}
		info.setOwner(dbShape.getOwner());
		shapeRepository.update(info);
		return info;
	}
	
	@PostMapping("/deleteShapeInfo/{id}")
	public String deleteShapeInfo(@PathVariable("id") String id) {
		ShapeInfo dbShape = shapeRepository.queryById(id);
		if(dbShape != null && !dbShape.getOwner().equals(getUserInfo().getUsername())) {
			throw new AccessDeniedException("无权访问");
		}
		shapeRepository.deleteById(id);
		return id;
	}
}
