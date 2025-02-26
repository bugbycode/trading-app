package com.bugbycode.trading_app;

import com.bugbycode.module.FibCode;

public class FibCodeTest {

	public static void main(String[] args) {
		
		FibCode[] codes = FibCode.values();
		
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];
			
			if(code.gt(FibCode.FIB1)) {
				continue;
			}
			
			System.out.println(code);
		}
	}

}
