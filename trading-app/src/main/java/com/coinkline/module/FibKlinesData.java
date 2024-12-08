package com.coinkline.module;

public class FibKlinesData<LCONIC_LOW_KLINES,LCONIC_HIGTH_KLINES> {

	private LCONIC_LOW_KLINES lconicLowPriceList;
	
	private LCONIC_HIGTH_KLINES lconicHighPriceList;

	public FibKlinesData(LCONIC_LOW_KLINES lconicLowPriceList, LCONIC_HIGTH_KLINES lconicHighPriceList) {
		this.lconicLowPriceList = lconicLowPriceList;
		this.lconicHighPriceList = lconicHighPriceList;
	}

	public LCONIC_LOW_KLINES getLconicLowPriceList() {
		return lconicLowPriceList;
	}

	public LCONIC_HIGTH_KLINES getLconicHighPriceList() {
		return lconicHighPriceList;
	}
	
	
}
