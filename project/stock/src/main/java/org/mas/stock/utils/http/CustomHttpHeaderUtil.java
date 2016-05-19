package org.mas.stock.utils.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CustomHttpHeaderUtil {
	private static final String X_SERIALNUM = "X-Serialnum";
	private static final String X_CLIENT_HASH = "X-Client-Hash";
	private static final String X_CLIENT_AGENT = "X-Client-Agent";
	private static final String X_CLIENT_ID = "X-Client-ID";
	private static final String X_CLIENT_VERSION = "X-Client-Version";
	private static final String X_PLATFORM_VERSION = "X-Platform-Version";
	private static final String X_APIVERSION = "X-APIVersion";
	private static final String X_CHANNEL_CODE = "X-Channel-Code";
	private static final String X_USER_ID = "X-User-ID";
	private static final String X_LONG_TOKEN = "X-Long-Token";
	private static final String X_RESULT_CODE = "X-Result-Code";

	private static String getRequestHeader(String headerName) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		return request.getHeader(headerName);
	}

	private static void setResponseHeader(String headerName, String headerValue) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletResponse response = attributes.getResponse();
		response.setHeader(headerName, headerValue);
	}

	/**
	 * 请求串号，时间戳
	 */
	public static String getSerialNum() {
		return getRequestHeader(X_SERIALNUM);
	}

	/**
	 * 请求串号，时间戳
	 */
	public static String getSerialNum(HttpServletRequest request) {
		return request.getHeader(X_SERIALNUM);
	}

	/**
	 * 基于serialnum等数据的数字摘要
	 */
	public static String getClientHash() {
		return getRequestHeader(X_CLIENT_HASH);
	}

	/**
	 * 基于serialnum等数据的数字摘要
	 */
	public static String getClientHash(HttpServletRequest request) {
		return request.getHeader(X_CLIENT_HASH);
	}

	/**
	 * 客户端UA:1024x768_samsung
	 */
	public static String getClientAgent() {
		return getRequestHeader(X_CLIENT_AGENT);
	}

	/**
	 * 客户端UA:1024x768_samsung
	 */
	public static String getClientAgent(HttpServletRequest request) {
		return request.getHeader(X_CLIENT_AGENT);
	}

	/**
	 * 设备唯一标识符
	 */
	public static String getClientId() {
		return getRequestHeader(X_CLIENT_ID);
	}

	/**
	 * 设备唯一标识符
	 */
	public static String getClientId(HttpServletRequest request) {
		return request.getHeader(X_CLIENT_ID);
	}

	/**
	 * 客户端版本号
	 */
	public static String getClientVersion() {
		return getRequestHeader(X_CLIENT_VERSION);
	}

	/**
	 * 客户端版本号
	 */
	public static String getClientVersion(HttpServletRequest request) {
		return request.getHeader(X_CLIENT_VERSION);
	}

	/**
	 * 客户端平台:android_422
	 */
	public static String getPlatformVersion() {
		return getRequestHeader(X_PLATFORM_VERSION);
	}

	/**
	 * 客户端平台:android_422
	 */
	public static String getPlatformVersion(HttpServletRequest request) {
		return request.getHeader(X_PLATFORM_VERSION);
	}

	/**
	 * API版本号：v1
	 */
	public static String getAPIVersion() {
		return getRequestHeader(X_APIVERSION);
	}

	/**
	 * API版本号：v1
	 */
	public static String getAPIVersion(HttpServletRequest request) {
		return request.getHeader(X_APIVERSION);
	}

	/**
	 * 渠道号：HB00001
	 */
	public static String getChannelCode() {
		return getRequestHeader(X_CHANNEL_CODE);
	}

	/**
	 * 渠道号：HB00001
	 */
	public static String getChannelCode(HttpServletRequest request) {
		return request.getHeader(X_CHANNEL_CODE);
	}

	/**
	 * 用户ID
	 */
	public static String getUserId() {
		return getRequestHeader(X_USER_ID);
	}

	/**
	 * 用户ID
	 */
	public static String getUserId(HttpServletRequest request) {
		return request.getHeader(X_USER_ID);
	}

	/**
	 * 长效Token
	 */
	public static String getLongToken() {
		return getRequestHeader(X_LONG_TOKEN);
	}

	/**
	 * 长效Token
	 */
	public static String getLongToken(HttpServletRequest request) {
		return request.getHeader(X_LONG_TOKEN);
	}

	/**
	 * 在消息头中返回请求串号
	 */
	public static void setSerialnum(String value) {
		setResponseHeader(X_SERIALNUM, value);
	}

	/**
	 * 在消息头中返回请求串号
	 */
	public static void setSerialnum(HttpServletResponse response, String value) {
		response.setHeader(X_SERIALNUM, value);
	}

	/**
	 * 在消息头中返回结果码
	 */
	public static void setResultCode(String value) {
		setResponseHeader(X_RESULT_CODE, value);
	}

	/**
	 * 在消息头中返回结果码
	 */
	public static void setResultCode(HttpServletResponse response, String value) {
		response.setHeader(X_RESULT_CODE, value);
	}

	/**
	 * 在消息头中返回长效Token
	 */
	public static void setLongToken(String value) {
		setResponseHeader(X_LONG_TOKEN, value);
	}

	/**
	 * 在消息头中返回长效Token
	 */
	public static void setLongToken(HttpServletResponse response, String value) {
		response.setHeader(X_LONG_TOKEN, value);
	}

	/**
	 * 在消息头中返回用户Id
	 */
	public static void setUserId(String value) {
		setResponseHeader(X_USER_ID, value);
	}

	/**
	 * 在消息头中返回用户Id
	 */
	public static void setUserId(HttpServletResponse response, String value) {
		response.setHeader(X_USER_ID, value);
	}

}
