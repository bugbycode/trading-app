package com.coinkline.module.binance;

import com.coinkline.module.ResultCode;

public class Result {

	private ResultCode result;
	
	private int code;
	
	private String msg;

	public Result(ResultCode result, int code, String msg) {
		this.result = result;
		this.code = code;
		this.msg = msg;
	}

	public ResultCode getResult() {
		return result;
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
	
	
}
