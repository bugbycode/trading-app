package com.util;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.PolicyType;

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
	
	/**
	 * 校验币种筛选策略 </br>
	 * 1、当selected为空时直接返回true </br>
	 * 2、当policyType为1且selected中包含pair时返回true </br>
	 * 3、当policyType为0且selected中包含pair时返回false </br>
	 * @param selected 已选中的币种信息 
	 * @param pair 校验的币种
	 * @param policyType 筛选策略 1：白名单，0：黑名单
	 * @return 返回true表示可交易，false表示不可交易
	 */
	public static boolean verifyPairPolicy(List<String> selected, String pair, int policyType) {
		boolean result = false;
		PolicyType type = PolicyType.valueOf(policyType);
		if(CollectionUtils.isEmpty(selected)) {
			result = true;
		} else if((type == PolicyType.ALLOW && verifyContainsPair(selected, pair))
				|| (type == PolicyType.DENY && !verifyContainsPair(selected, pair))) {
			result = true;
		}
		return result;
	}
	
	
}
