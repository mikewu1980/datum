package org.mas.stock.usl.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	protected HttpServletRequest request;

	@Resource
	protected HttpServletResponse response;

	// public UserVO getCurrentUserFromSession() {
	// return (UserVO) request.getSession().getAttribute(GlobalConstants.SESSION_KEY_USER);
	// }

	// public Long currentUserId() {
	// return ((UserVO) request.getSession().getAttribute(GlobalConstants.SESSION_KEY_USER))
	// .getId();
	// }

	// public void setUserToSession(UserVO userInfo) {
	// request.getSession().setAttribute(GlobalConstants.SESSION_KEY_USER, userInfo);
	// }

	/**
	 * 获取session对象
	 */
	protected HttpSession getSession() {
		return request.getSession();
	}
}
