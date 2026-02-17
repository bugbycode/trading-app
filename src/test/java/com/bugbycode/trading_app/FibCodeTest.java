package com.bugbycode.trading_app;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;

public class FibCodeTest {

	public static void main(String[] args) {
		
		FibInfo fibInfo = new FibInfo(1, 100, 2, FibLevel.LEVEL_0);
		
		FibCode[] codes = FibCode.values();
		
		for(int offset = 0;offset < codes.length;offset++) {
			
			FibCode code = codes[offset];
			System.out.println(code.getValue() + " => " + fibInfo.getNextFibCode(code).getValue() + " ~ " + fibInfo.getTakeProfit_v2(code).getValue());
		}
		
		/*for(int index = 0; index < codes.length - 1; index++) {
			FibCode current = codes[index];
			FibCode parent = codes[index + 1];
			if(current == FibCode.FIB786) {
				parent = FibCode.FIB618;
			}
			System.out.println(codes[index] + " -> " + parent);
		}*/
	}

}
