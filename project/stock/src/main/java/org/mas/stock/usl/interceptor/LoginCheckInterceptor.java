package org.mas.stock.usl.interceptor;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class LoginCheckInterceptor extends HandlerInterceptorAdapter {
	private final Logger logger = LoggerFactory.getLogger(LoginCheckInterceptor.class);

	@Value("#{SYSTEM_CONFIG['user.token.validDays']}")
	private int validDays;

	@Value("#{SYSTEM_CONFIG['user.token.md5Key']}")
	private String md5Key;

	@Value("#{SYSTEM_CONFIG['system.login.uncheck.switch']}")
	private boolean loginUnCheckSwitch;

	// @Resource
	// private TokenCache tokenCache;

	// @Resource
	// private UserInfoCache userInfoCache;

	/**
	 * 在业务处理器处理请求之前被调用 如果返回false 从当前的拦截器往回执行所有拦截器的afterCompletion(),再退出拦截器链 如果返回true 执行下一个拦截器,直到所有的拦截器都执行完毕
	 * 再执行被拦截的Controller 然后进入拦截器链, 从最后一个拦截器往回执行所有的postHandle() 接着再从最后一个拦截器往回执行所有的afterCompletion()
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// String debugUserId = request.getParameter("debugUserId");
		// if (loginUnCheckSwitch && StringUtils.isNotBlank(debugUserId) && NumberUtils.isDigits(debugUserId)) {
		// UserVO user = userInfoCache.get(Long.parseLong(debugUserId));
		// if (user != null)
		// request.getSession().setAttribute(GlobalConstants.SESSION_KEY_USER, user);
		// return true;
		// }
		//
		// String userId = CustomHttpHeaderUtil.getUserId(request);
		// String clientId = CustomHttpHeaderUtil.getClientId(request);
		// String longToken = CustomHttpHeaderUtil.getLongToken(request);
		//
		// // 请求参数非法
		// if (StringUtils.isAnyBlank(userId)) {
		// logger.warn("userId is null, requestUri = {}", request.getRequestURI());
		// throw new BusinessLogicException(ExceptionConstants.USERS_USERID_IS_NULL);
		// }
		//
		// if (StringUtils.isAnyBlank(clientId)) {
		// logger.warn("clientId is null, requestUri = {}", request.getRequestURI());
		// throw new BusinessLogicException(ExceptionConstants.USER_CLIENTID_IS_NULL);
		// }
		//
		// // 根据userId获取longToken、clientId等缓信息
		// TokenVO tokenVO = tokenCache.get(Long.parseLong(userId));
		// if (tokenVO == null) {
		// logger.warn("tokenCache is null, requestUri = {}", request.getRequestURI());
		// throw new BusinessLogicException(ExceptionConstants.USERS_TOKEN_CACHE_IS_NULL);
		// }
		//
		// // session为空:根据token鉴权;session不为空:根据session鉴权
		// UserVO userInfo = (UserVO) request.getSession().getAttribute(GlobalConstants.SESSION_KEY_USER);
		// if (userInfo == null) {
		// double tokenUsedDays = tokenVO.getTokenUsedDays();
		//
		// // 长效token为空或用户token缓存过期，表示用户未登陆
		// if (StringUtils.isBlank(longToken) || !tokenVO.isTokenValid(validDays)) {
		// logger.warn("User is not login, sessionid = {}, requestUri = {}", request.getSession().getId(),
		// request.getRequestURI());
		// throw new BusinessLogicException(ExceptionConstants.USERS_NOT_LOGIN);
		// }
		//
		// // 长效token与用户缓存token不相同，表示用户已在其它设备登录
		// if (!longToken.equals(tokenVO.getLongToken())) {
		// logger.warn("User is login at other terminal, longToken = {}, cacheToken = {}", longToken,
		// tokenVO.getLongToken());
		// throw new BusinessLogicException(ExceptionConstants.USERS_LOGIN_AT_OTHER_TERMINAL);
		// }
		//
		// UserVO user = userInfoCache.get(Long.parseLong(userId));
		//
		// // 判断用户是否存在
		// if (user == null) {
		// throw new BusinessLogicException(ExceptionConstants.USERS_QUERY_USER_NOT_EXISTS);
		// }
		//
		// // 判断用户是否被冻结
		// if (user.isFreezing()) {
		// throw new BusinessLogicException(ExceptionConstants.USER_IS_FROZEN);
		// }
		//
		// // 长效token与用户缓存token相同，可登陆
		// longToken = this.getNewLongToken(userId, clientId, longToken, tokenUsedDays);
		//
		// CustomHttpHeaderUtil.setLongToken(response, longToken);
		// request.getSession().setAttribute(GlobalConstants.SESSION_KEY_USER, user);
		// } else {
		// // 客户端设备号与用户缓存设备号不同，表示用户已在其它设备登录
		// if (!clientId.equals(tokenVO.getClientId())) {
		// logger.warn("User is login at other terminal, longToken = {}, cacheToken = {}", longToken,
		// tokenVO.getLongToken());
		// throw new BusinessLogicException(ExceptionConstants.USERS_LOGIN_AT_OTHER_TERMINAL);
		// }
		// }

		return true;
	}

	/**
	 * 根据缓存token生成天数，判断是否需要生成新的longToken,得到有效的longToken
	 * 
	 * @param userId
	 * @param clientId
	 * @param longToken
	 * @param tokenUsedDays
	 * @return
	 */
	private String getNewLongToken(String userId, String clientId, String longToken, double tokenUsedDays) {
		if (tokenUsedDays * 2 > validDays) {
			Date currentDate = new Date();
			String sourceStr = new StringBuilder(64).append(userId).append(clientId).append(currentDate.getTime())
					.append(md5Key).toString();

			// longToken = DigestUtils.md5Hex(sourceStr);
			// TokenVO newToken = new TokenVO();
			// newToken.setCreateDate(currentDate);
			// newToken.setLongToken(longToken);
			// newToken.setClientId(clientId);
			// tokenCache.put(Long.parseLong(userId), newToken);
		}

		return longToken;
	}
}
