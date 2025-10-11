package com.util;

import java.util.List;

import org.springframework.util.CollectionUtils;

public class PairPolicyUtil {

	/**
	 * 检查 pairPolicySelected 中是否包含 pair
	 * @param pairPolicySelected
	 * @param pair
	 * @return 如果 policySelected 为空或包含 pair 则返回 true 否则返回 false
	 */
	public static boolean verifyContainsPair(List<String> pairPolicySelected, String pair) {
		boolean result = false;
		if(CollectionUtils.isEmpty(pairPolicySelected)) {
			result = true;
		} else {
			for(String p : pairPolicySelected) {
				if(p.trim().equals(pair)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
}
