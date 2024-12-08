package com.coinkline.module.result;

public class DeclineAndStrength<BOOL,LONG_OR_SHORT> {
	
	private BOOL verify;
	
	private LONG_OR_SHORT longOrShort;

	public DeclineAndStrength(BOOL verify, LONG_OR_SHORT longOrShort) {
		this.verify = verify;
		this.longOrShort = longOrShort;
	}

	public BOOL getVerify() {
		return verify;
	}

	public LONG_OR_SHORT getLongOrShort() {
		return longOrShort;
	}
	
	
}
