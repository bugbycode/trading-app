package com.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.binance.ContractType;
import com.bugbycode.module.binance.SymbolExchangeInfo;

public class CoinPairSet extends HashSet<SymbolExchangeInfo> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Inerval inerval;
	
	private ContractType contractType;
	
	private Set<String> finishPair = Collections.synchronizedSet(new HashSet<String>());
	
	public CoinPairSet(Inerval inerval, ContractType contractType) {
		this.inerval = inerval;
		this.contractType = contractType;
	}

	public Inerval getInerval() {
		return inerval;
	}

	public String getStreamName() {
		StringBuffer buffer = new StringBuffer();
		Iterator<SymbolExchangeInfo> it = this.iterator();
		while(it.hasNext()) {
			if(buffer.length() > 0) {
				buffer.append("/");
			}
			try {
				
				SymbolExchangeInfo info = it.next();
				
				String pair = info.getSymbol().toLowerCase();
				if(this.contractType == ContractType.E_OPTIONS) {
					buffer.append(pair + "@kline_" + inerval.getDescption());
				} else {
					String contractTypeStr = info.getContractType().getValue().toLowerCase();
					buffer.append(URLEncoder.encode(pair, "UTF-8") + "_" + contractTypeStr + "@continuousKline_" + inerval.getDescption());
				}
			}catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return buffer.toString();
	}
	
	
	@Override
	public boolean add(SymbolExchangeInfo e) {
		if(isFull()) {
			return false;
		}
		return super.add(e);
	}

	public boolean isFull() {
		return this.size() == 10;
	}

	public boolean isEmpty() {
		return this.size() == 0;
	}
	
	@Override
	public void clear() {
		super.clear();
		this.finishPair.clear();
	}
	
	public boolean addFinishPair(String pair) {
		if(this.finishPair.contains(pair)) {
			return false;
		}
		return this.finishPair.add(pair);
	}
	
	public boolean isFinish() {
		return this.finishPair.size() == this.size();
	}
}
