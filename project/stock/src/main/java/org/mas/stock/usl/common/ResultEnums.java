package org.mas.stock.usl.common;

public enum ResultEnums {
	/** 请求参数不合法 */
	SYSTEM_ILLEGAL_REQUEST_PARAMETERS(-900001, "请求参数不合法"),
	/** 请求参数缺失 */
	SYSTEM_MISSING_REQUEST_PARAMETERS(-900002, "请求参数缺失"),
	/** 必要的HTTP头缺失 */
	SYSTEM_MISSING_REQUIRED_HTTP_HEADER(-900003, "必要的HTTP头缺失"),

	/** 调用远程服务未找到 */
	SYSTEM_REMOTE_SERVICE_NOT_FOUND(-970001, "调用远程服务未找到"),
	/** 调用远程服务超时 */
	SYSTEM_REMOTE_SERVICE_TIME_OUT(-970002, "调用远程服务超时"),

	/** SpringMVC异常：Bean绑定异常 */
	SYSTEM_MVC_BIND_EXCEPTION(-980001, "服务器内部系统异常"),
	/** SpringMVC异常：当没有合适的编辑器或转换器可以找到一个Bean属性时抛出异常。 */
	SYSTEM_MVC_CONVERSION_NOT_SUPPORTED_EXCEPTION(-980002, "服务器内部系统异常"),
	/** SpringMVC异常：当请求处理程序不能产生一个客户端可以接受的响应时抛出异常。 */
	SYSTEM_MVC_HTTP_MEDIATYPE_NOT_ACCEPTABLE_EXCEPTION(-980003, "服务器内部系统异常"),
	/** SpringMVC异常：请求的某类HTTP方法（Get、Post等）到不支持该方法的处理程序 */
	SYSTEM_MVC_HTTP_MEDIATYPE_NOT_SUPPORTED_EXCEPTION(-980004, "服务器内部系统异常"),
	/** SpringMVC异常：请求的消息不可读 */
	SYSTEM_MVC_HTTP_MESSAGE_NOT_READABLE_EXCEPTION(-980005, "服务器内部系统异常"),
	/** SpringMVC异常：请求的消息不能写 */
	SYSTEM_MVC_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION(-980006, "服务器内部系统异常"),
	/** SpringMVC异常：请求的处理程序不支持特定的HTTP方法（Get、Post等） */
	SYSTEM_MVC_HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION(-980007, "服务器内部系统异常"),
	/** SpringMVC异常：查找请求的处理程序时抛出的异常 */
	SYSTEM_MVC_NO_HANDLER_FOUND_EXCEPTION(-980008, "服务器内部系统异常"),
	/** SpringMVC异常：某个HTTP请求没有对应的处理程序 */
	SYSTEM_MVC_NO_SUCH_REQUEST_HANDLING_METHOD_EXCEPTION(-980009, "服务器内部系统异常"),
	/** SpringMVC异常：请求参数验证失败 */
	SYSTEM_MVC_METHOD_ARGUMENT_NOT_VALID_EXCEPTION(-980010, "服务器内部系统异常"),
	/** SpringMVC异常：某个HTTP请求没有对应的处理程序 */
	SYSTEM_MVC_MISSING_PATH_VARIABLE_EXCEPTION(-980011, "服务器内部系统异常"),
	/** SpringMVC异常：某个HTTP请求参数缺失 */
	SYSTEM_MVC_MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION(-980012, "服务器内部系统异常"),
	/** SpringMVC异常：带附件的HTTP请求缺失了附件部分的数据 */
	SYSTEM_MVC_MISSING_SERVLET_REQUEST_PART_EXCEPTION(-980013, "服务器内部系统异常"),
	/** SpringMVC异常：HTTP请求绑定异常 */
	SYSTEM_MVC_SERVLET_REQUEST_BINDING_EXCEPTION(-980014, "服务器内部系统异常"),
	/** SpringMVC异常：绑定Bean类型匹配异常 */
	SYSTEM_MVC_TYPE_MISMATCH_EXCEPTION(-980015, "服务器内部系统异常"),

	/** 数据库异常 */
	SYSTEM_SQL_EXCEPTION(-990001, "数据库异常"),

	/** 其它系统异常 */
	SYSTEM_OTHER_EXCEPTION(-999999, "其他系统异常");

	private ResultEnums(int value, String text) {
		this.value = value;
		this.text = text;
	}

	private final int value;
	private final String text;

	public int getValue() {
		return value;
	}

	public String getText() {
		return text;
	}

}
