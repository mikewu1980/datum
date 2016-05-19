package org.mas.stock.usl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mas.stock.usl.common.JsonpFastJsonHttpMessageConverter;
import org.mas.stock.usl.common.ResponseEntity;
import org.mas.stock.usl.common.ResultEnums;
import org.mas.stock.utils.http.CustomHttpHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

/**
 * 这个类用来handle整个系统的异常或正常返回对象，包装成Response对象返回给调用方
 */
@Component
@ControllerAdvice(basePackages = {"org.mas.stock.usl.controller"})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ResponseHandler implements HandlerExceptionResolver, ResponseBodyAdvice<Object> {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String JSONP_FUNCTION_KEY = "callback";

	/** 这里定义的是需要返回详细错误的异常 */
	private Map<String, ResultEnums> exceptionMappings = new HashMap<>();

	@PostConstruct
	private void initExceptionMappings() {
		// SpringMVC 的一些异常
		exceptionMappings.put(BindException.class.getName(), ResultEnums.SYSTEM_MVC_BIND_EXCEPTION);
		exceptionMappings.put(ConversionNotSupportedException.class.getName(),
				ResultEnums.SYSTEM_MVC_CONVERSION_NOT_SUPPORTED_EXCEPTION);
		exceptionMappings.put(HttpMediaTypeNotAcceptableException.class.getName(),
				ResultEnums.SYSTEM_MVC_HTTP_MEDIATYPE_NOT_ACCEPTABLE_EXCEPTION);
		exceptionMappings.put(HttpMediaTypeNotSupportedException.class.getName(),
				ResultEnums.SYSTEM_MVC_HTTP_MEDIATYPE_NOT_SUPPORTED_EXCEPTION);
		exceptionMappings.put(HttpMessageNotReadableException.class.getName(),
				ResultEnums.SYSTEM_MVC_HTTP_MESSAGE_NOT_READABLE_EXCEPTION);
		exceptionMappings.put(HttpMessageNotWritableException.class.getName(),
				ResultEnums.SYSTEM_MVC_HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION);
		exceptionMappings.put(HttpRequestMethodNotSupportedException.class.getName(),
				ResultEnums.SYSTEM_MVC_HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION);
		exceptionMappings.put(NoHandlerFoundException.class.getName(),
				ResultEnums.SYSTEM_MVC_NO_HANDLER_FOUND_EXCEPTION);
		exceptionMappings.put(NoSuchRequestHandlingMethodException.class.getName(),
				ResultEnums.SYSTEM_MVC_NO_SUCH_REQUEST_HANDLING_METHOD_EXCEPTION);
		exceptionMappings.put(MethodArgumentNotValidException.class.getName(),
				ResultEnums.SYSTEM_MVC_METHOD_ARGUMENT_NOT_VALID_EXCEPTION);
		exceptionMappings.put(MissingPathVariableException.class.getName(),
				ResultEnums.SYSTEM_MVC_MISSING_PATH_VARIABLE_EXCEPTION);
		exceptionMappings.put(MissingServletRequestParameterException.class.getName(),
				ResultEnums.SYSTEM_MVC_MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION);
		exceptionMappings.put(MissingServletRequestPartException.class.getName(),
				ResultEnums.SYSTEM_MVC_MISSING_SERVLET_REQUEST_PART_EXCEPTION);
		exceptionMappings.put(ServletRequestBindingException.class.getName(),
				ResultEnums.SYSTEM_MVC_SERVLET_REQUEST_BINDING_EXCEPTION);
		exceptionMappings.put(TypeMismatchException.class.getName(), ResultEnums.SYSTEM_MVC_TYPE_MISMATCH_EXCEPTION);
		// SpringMVC 的一些异常
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		ModelAndView modelAndView = new ModelAndView();
		Object extra = null;
		String name = ex.getClass().getName();
		ResultEnums resultEnum = exceptionMappings.get(name);

		if (null == resultEnum) {
			if (ex instanceof SQLException || ex instanceof DataAccessException) {
				resultEnum = ResultEnums.SYSTEM_SQL_EXCEPTION;
				// } else if (ex instanceof UndeclaredThrowableException) {
				// resultEnum = ResultEnums.SYSTEM_REMOTE_SERVICE_NOT_FOUND;
				// } else if (StringUtils.startsWith(ex.getMessage(), "HSFTimeOutException-Timeout")) {
				// // 阿里云的分布服务调用超时
				// resultEnum = ResultEnums.SYSTEM_REMOTE_SERVICE_TIME_OUT;
				// } else if (ex instanceof IllegalStateException
				// && StringUtils.endsWith(ex.getMessage(),
				// "Consider declaring it as object wrapper for the corresponding primitive type.")) {
				// resultEnum = ResultEnums.SYSTEM_MISSING_REQUEST_PARAMETERS;
			} else {
				resultEnum = ResultEnums.SYSTEM_OTHER_EXCEPTION;
			}
		}

		ResponseEntity portalResponseEntity = new ResponseEntity(resultEnum, extra);
		String jsonpFunction = request.getParameter(JSONP_FUNCTION_KEY);
		if (jsonpFunction != null) {
			response.setContentType("application/javascript");
			portalResponseEntity.setJsonpFunction(jsonpFunction);
		}

		CustomHttpHeaderUtil.setResultCode(response, String.valueOf(portalResponseEntity.getErrorCode()));
		CustomHttpHeaderUtil.setSerialnum(response, CustomHttpHeaderUtil.getSerialNum(request));

		modelAndView.addObject(portalResponseEntity);

		return modelAndView;
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return JsonpFastJsonHttpMessageConverter.class.isAssignableFrom(converterType);
	}

	/**
	 * 将接口返回的对象统一包装成ResponseEntity类的实例并增加jsonp的支持
	 * 
	 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice#beforeBodyWrite(java.lang.Object,
	 *      org.springframework.core.MethodParameter, org.springframework.http.MediaType, java.lang.Class,
	 *      org.springframework.http.server.ServerHttpRequest, org.springframework.http.server.ServerHttpResponse)
	 */
	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {

		ResponseEntity portalResponseEntity = new ResponseEntity(body);

		HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
		String jsonpFunction = servletRequest.getParameter(JSONP_FUNCTION_KEY);
		if (jsonpFunction != null) {
			response.getHeaders().setContentType(new MediaType("application", "javascript"));
			portalResponseEntity.setJsonpFunction(jsonpFunction);
		}

		HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
		CustomHttpHeaderUtil.setResultCode(servletResponse, String.valueOf(portalResponseEntity.getErrorCode()));
		CustomHttpHeaderUtil.setSerialnum(servletResponse, CustomHttpHeaderUtil.getSerialNum(servletRequest));

		return portalResponseEntity;
	}
}
