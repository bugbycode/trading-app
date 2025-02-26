package com.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {

	/**
	 * 获取客户端IP地址
	 * 
	 * @param request
	 * @return
	 */
	public static String parseClientIp(HttpServletRequest request) {
		// 尝试从 "X-Forwarded-For" 头中获取客户端真实 IP 地址
        String clientIp = request.getHeader("X-Forwarded-For");
        if (StringUtil.isEmpty(clientIp)) {
            // 如果没有 "X-Forwarded-For" 头，直接获取请求的 remote address
            clientIp = request.getRemoteAddr();
        } else {
            // 如果存在多个 IP，取第一个（客户端的 IP）
            clientIp = clientIp.split(",")[0];
        }
        return clientIp;
	}
}
