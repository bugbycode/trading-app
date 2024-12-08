package com.coinkline.webapp.controller.shape;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coinkline.module.Inerval;
import com.coinkline.module.LongOrShortType;
import com.coinkline.module.ResultCode;
import com.coinkline.module.ShapeInfo;
import com.coinkline.module.user.User;
import com.coinkline.repository.shape.ShapeRepository;
import com.coinkline.service.klines.KlinesService;
import com.coinkline.service.shape.ShapeService;
import com.coinkline.webapp.controller.base.BaseController;
import com.util.page.SearchResult;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/shape")
public class ShapeController extends BaseController{

	private final Logger logger = LogManager.getLogger(ShapeController.class);
	
	@Resource
	private ShapeRepository shapeRepository;
	
	@Resource
	private ShapeService shapeService;
	
	@Resource
	private KlinesService klinesService;
	
	@GetMapping("/getAllShapeInfo")
	public List<ShapeInfo> getAllShapeInfo(){
		User user = getUserInfo();
		return shapeRepository.queryByOwner(user.getUsername());
	}
	
	@PostMapping("/saveShapeInfo")
	public ShapeInfo saveShapeInfo(@RequestBody ShapeInfo info) {
		info.setOwner(getUserInfo().getUsername());
		
		String price = klinesService.getClosePrice(info.getSymbol(), Inerval.INERVAL_15M);
		info.setCreateTime(new Date().getTime());
		info.setPrice(price);
		
		//价格坐标
		JSONArray pointsJsonArray = new JSONArray(info.getPoints());
		if(pointsJsonArray.length() > 0) {
			JSONObject points = pointsJsonArray.getJSONObject(0);
			double p = points.getDouble("price");
			//long time = points.getLong("time");
			double createPrice = info.getPriceDoubleValue();
			LongOrShortType type = LongOrShortType.LONG;
			if(createPrice < p) {//做空
				type = LongOrShortType.SHORT;
			} else if(createPrice > p) {
				type = LongOrShortType.LONG;
			}
			info.setLongOrShortType(type.getValue());
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
		
		String price = klinesService.getClosePrice(info.getSymbol(), Inerval.INERVAL_15M);
		
		info.setPrice(price);
		info.setCreateTime(dbShape.getCreateTime());
		info.setUpdateTime(new Date().getTime());
		info.setLongOrShortType(dbShape.getLongOrShortType());
		
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
	
	@PostMapping("/updateLongOrShortType")
	public String updateLongOrShortType(@RequestBody ShapeInfo info) {
		ResultCode code = ResultCode.SUCCESS;
		JSONObject json = new JSONObject();
		
		ShapeInfo dbShape = shapeRepository.queryById(info.getId());
		if(dbShape == null || !dbShape.getOwner().equals(getUserInfo().getUsername())) {
			throw new AccessDeniedException("无权访问");
		}
		
		LongOrShortType type = LongOrShortType.resolve(info.getLongOrShortType());
		
		shapeRepository.updateLongOrShortTypeById(info.getId(), type.getValue());
		
		json.put("message", "修改成功");
		json.put("code", code.getCode());
		
		return json.toString();
	}
	
	@GetMapping("/query")
	public SearchResult<ShapeInfo> query(
			@RequestParam(defaultValue = "",name = "symbol")
			String symbol,
			@RequestParam(defaultValue = "0",name = "startIndex") 
			long skip,
			@RequestParam(defaultValue = "10",name = "limit")
			int limit){
		User user = getUserInfo();
		return shapeService.query(user.getUsername(), symbol, skip, limit);
	}
}
