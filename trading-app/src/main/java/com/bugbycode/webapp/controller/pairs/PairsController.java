package com.bugbycode.webapp.controller.pairs;

import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugbycode.config.AppConfig;
import com.bugbycode.webapp.controller.base.BaseController;

@RestController
@RequestMapping("/pairs")
public class PairsController extends BaseController{

	@GetMapping("/getPairs")
	public Set<String> getPairs(){
		return AppConfig.PAIRS;
	}
}
