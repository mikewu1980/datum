package org.mas.stock.usl.interceptor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ParameterCheckInterceptor extends HandlerInterceptorAdapter {
	private ParameterChecker offsetChecker = new OffsetChecker();
	private ParameterChecker limitChecker = new LimitChecker();

	List<ParameterChecker> parameterCheckers = new ArrayList<>();

	{
		parameterCheckers.add(offsetChecker);
		parameterCheckers.add(limitChecker);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		for (ParameterChecker parameterChecker : parameterCheckers) {
			if (!parameterChecker.check(request.getParameter(parameterChecker.getParameterKey()))) {
				// throw new BusinessLogicException(ExceptionConstants.SYSTEM_ILLEGAL_REQUEST_PARAMETERS);
				throw new Exception("SYSTEM_ILLEGAL_REQUEST_PARAMETERS");
			}
		}

		return true;
	}

	interface ParameterChecker {
		boolean check(String parameter);

		String getParameterKey();
	}

	private class OffsetChecker implements ParameterChecker {
		private static final long MIN_OFFSET = 0;

		@Override
		public boolean check(String parameter) {
			return StringUtils.isBlank(parameter) || (NumberUtils.toLong(parameter, -1L) >= MIN_OFFSET);
		}

		@Override
		public String getParameterKey() {
			return "offset";
		}
	}

	private class LimitChecker implements ParameterChecker {
		private static final long MAX_LIMIT = 1000;
		private static final long MIN_LIMIT = 1;

		@Override
		public boolean check(String parameter) {
			long limit;
			return StringUtils.isBlank(parameter)
					|| ((limit = NumberUtils.toLong(parameter, 0L)) >= MIN_LIMIT && limit <= MAX_LIMIT);
		}

		@Override
		public String getParameterKey() {
			return "limit";
		}
	}

}
