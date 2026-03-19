package com.bugbycode.webapp.controller.openInterestHist;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugbycode.module.open_interest.OpenInterestHist;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;
import com.bugbycode.webapp.controller.base.BaseController;

@RestController
@RequestMapping("/openInterestHist")
public class OpenInterestHistController extends BaseController {

	@Autowired
	private OpenInterestHistRepository openInterestHistRepository;
	
	@GetMapping("/query")
	public List<OpenInterestHist> query() {
		return openInterestHistRepository.query();
	}
}
