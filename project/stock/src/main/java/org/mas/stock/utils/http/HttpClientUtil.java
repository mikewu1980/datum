package org.mas.stock.utils.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * httpclient工具类
 * <ul>
 * <li>groupId:org.apache.httpcomponents</li>
 * <li>artifactId:httpclient</li>
 * <li>version:4.3.x</li>
 * </ul>
 * 
 * @author wuzhipeng
 */
public class HttpClientUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	private static PoolingHttpClientConnectionManager connectionManager = null;
	private static CloseableHttpClient httpclient = null;
	private static String patternInfo = "HttpClientUtil.{} --->> [{}] url:{}, data:{}";
	private static Charset defaultCharset = Consts.UTF_8;
	private static int defaultTimeout = 5;
	private static String boundary = "----WebKitFormBoundaryZKakoyzRwD1QbdFB";

	static {
		try {
			// TODO 临时加载log4j配置
			// DOMConfigurator
			// .configure("C:\\work\\git_repository\\aladdin-server1\\qc-webapp\\src\\main\\resources\\log4j.xml");

			// SSLContext sslContext = SSLContexts.createSystemDefault();
			SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
			sslContextBuilder.useProtocol("TLSv1");
			sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLContext sslContext = sslContextBuilder.build();
			// SSLContext sslContext = SSLContexts.custom().useTLS().build();
			sslContext.init(null, new TrustManager[] {new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
				}

				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
				}
			}}, null);

			RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
			registryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
			registryBuilder.register("https", new SSLConnectionSocketFactory(sslContext));
			Registry<ConnectionSocketFactory> socketFactoryRegistry = registryBuilder.build();

			connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			HttpClientBuilder httpClientBuilder = HttpClients.custom();
			httpClientBuilder.setConnectionManager(connectionManager);
			httpClientBuilder.setDefaultHeaders(getDefaultHeader());
			httpclient = httpClientBuilder.build();

			// Create socket configuration
			org.apache.http.config.SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
			socketConfigBuilder.setTcpNoDelay(true);
			SocketConfig socketConfig = socketConfigBuilder.build();
			connectionManager.setDefaultSocketConfig(socketConfig);

			// Create message constraints
			org.apache.http.config.MessageConstraints.Builder messageConstraintsBuilder = MessageConstraints.custom();
			messageConstraintsBuilder.setMaxHeaderCount(200);
			messageConstraintsBuilder.setMaxLineLength(2000);
			MessageConstraints messageConstraints = messageConstraintsBuilder.build();

			// Create connection configuration
			org.apache.http.config.ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom();
			connectionConfigBuilder.setMalformedInputAction(CodingErrorAction.IGNORE);
			connectionConfigBuilder.setUnmappableInputAction(CodingErrorAction.IGNORE);
			connectionConfigBuilder.setCharset(Consts.UTF_8);
			connectionConfigBuilder.setMessageConstraints(messageConstraints);
			ConnectionConfig connectionConfig = connectionConfigBuilder.build();

			connectionManager.setDefaultConnectionConfig(connectionConfig);
			connectionManager.setMaxTotal(200);
			connectionManager.setDefaultMaxPerRoute(20);
		} catch (KeyStoreException e) {
			logger.error(e.getMessage(), e);
		} catch (KeyManagementException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private static List<org.apache.http.Header> getDefaultHeader() {
		ArrayList<org.apache.http.Header> headers = new ArrayList<org.apache.http.Header>();

		String userAgent = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36";
		org.apache.http.Header header1 = new BasicHeader(HttpHeaders.USER_AGENT, userAgent);
		headers.add(header1);

		String cacheControl = "max-age=0";
		// String cacheControl = "no-cache";
		org.apache.http.Header header2 = new BasicHeader(HttpHeaders.CACHE_CONTROL, cacheControl);
		headers.add(header2);

		return headers;
	}

	/**
	 * 超时时间（单位：秒）
	 * 
	 * @param overtime
	 * @return
	 */
	private static RequestConfig getRequestConfig(int overtime) {
		Builder requestConfigBuilder = RequestConfig.custom();

		requestConfigBuilder.setSocketTimeout(overtime);
		requestConfigBuilder.setConnectTimeout(overtime);
		requestConfigBuilder.setConnectionRequestTimeout(overtime);
		requestConfigBuilder.setExpectContinueEnabled(false);

		RequestConfig requestConfig = requestConfigBuilder.build();

		return requestConfig;
	}

	/**
	 * 提交请求
	 * 
	 * @param request
	 *            请求实体
	 * @param charset
	 *            字符集
	 * @return
	 */
	private static String submitRequest(HttpUriRequest request, Charset charset) {
		String responseContent = null;
		CloseableHttpResponse response = null;

		try {
			// 发出请求
			response = httpclient.execute(request);

			StatusLine statusLine = response.getStatusLine();
			// 检查响应状态
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				String pattern = "HttpClientUtil.submitRequest --->> status:{}, url:{}, data:{}";
				logger.error(pattern, statusLine, request.getURI(), request.getRequestLine());
			}

			// 获取响应实体
			HttpEntity responseEntity = response.getEntity();

			try {
				if (responseEntity != null) {
					// 解析响应正文
					responseContent = EntityUtils.toString(responseEntity, charset);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				// 销毁
				EntityUtils.consume(responseEntity);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return responseContent;
	}

	private static byte[] submitRequest(HttpUriRequest request) {
		byte[] responseByteArray = null;
		CloseableHttpResponse response = null;

		try {
			// 发出请求
			response = httpclient.execute(request);

			StatusLine statusLine = response.getStatusLine();
			// 检查响应状态
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				String pattern = "HttpClientUtil.submitRequest --->> status:{}, url:{}, data:{}";
				logger.error(pattern, statusLine, request.getURI(), request.getRequestLine());
			}

			// 获取响应实体
			HttpEntity responseEntity = response.getEntity();

			try {
				if (responseEntity != null) {
					// 解析响应正文
					InputStream input = responseEntity.getContent();
					responseByteArray = IOUtils.toByteArray(input);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				// 销毁
				EntityUtils.consume(responseEntity);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return responseByteArray;
	}

	/**
	 * POST方式提交JSON数据
	 * 
	 * @param url
	 *            请求地址
	 * @param data
	 *            提交数据（json字符串）
	 * @param characterSet
	 *            字符集
	 * @param timeout
	 *            超时时间(单位：秒)
	 * @return
	 */
	public static String postJsonBody(String url, String data, Charset characterSet, int timeout) {
		// 入参检查
		if (StringUtils.isBlank(url)) {
			return null;
		}

		String json = data;
		if (StringUtils.isBlank(data)) {
			json = "";
		}

		int overtime = timeout * 1000;
		if (timeout <= 0) {
			overtime = defaultTimeout * 1000;
		}

		Charset charset = characterSet;
		if (null == charset) {
			charset = defaultCharset;
		}

		String methodName = "postJsonBody";
		String responseContent = null;
		HttpPost post = new HttpPost(url);

		// 设置头信息
		// post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		post.setHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));

		// 设置请求配置
		post.setConfig(getRequestConfig(overtime));

		try {
			// 设置请求数据
			StringEntity requestEntity = new StringEntity(json, charset);
			post.setEntity(requestEntity);

			if (logger.isDebugEnabled()) {
				logger.debug(patternInfo, methodName, "request", url, json);
			}

			// 发出请求
			responseContent = submitRequest(post, charset);
		} finally {
			post.releaseConnection();
		}

		if (logger.isDebugEnabled()) {
			logger.debug(patternInfo, methodName, "response", url, responseContent);
		}

		return responseContent;
	}

	/**
	 * POST方式提交表单数据
	 * 
	 * @param url
	 *            请求地址
	 * @param parameters
	 *            数据集合
	 * @param characterSet
	 *            字符集
	 * @param timeout
	 *            超时时间（单位：秒）
	 * @return
	 */
	public static String postForm(String url, Collection<NameValuePair> parameters, Charset characterSet, int timeout) {
		// 入参检查
		if (StringUtils.isBlank(url)) {
			return null;
		}

		Collection<NameValuePair> params = parameters;
		if (null == params) {
			params = new ArrayList<NameValuePair>();
		}

		int overtime = timeout * 1000;
		if (timeout <= 0) {
			overtime = defaultTimeout * 1000;
		}

		Charset charset = characterSet;
		if (null == charset) {
			charset = defaultCharset;
		}

		String methodName = "postForm";
		String responseContent = null;
		HttpPost post = new HttpPost(url);

		// 设置请求配置
		post.setConfig(getRequestConfig(overtime));

		try {
			// 设置请求数据
			UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(params, charset);
			post.setEntity(requestEntity);

			if (logger.isDebugEnabled()) {
				String data = JSON.toJSONString(params, SerializerFeature.WriteDateUseDateFormat);
				logger.debug(patternInfo, methodName, "request", url, data);
			}

			// 发出请求
			responseContent = submitRequest(post, charset);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			post.releaseConnection();
		}

		if (logger.isDebugEnabled()) {
			logger.debug(patternInfo, methodName, "response", url, responseContent);
		}

		return responseContent;
	}

	/**
	 * POST提交表单数据及附件
	 * 
	 * @param url
	 * @param parameters
	 * @param attachments
	 * @param characterSet
	 * @param timeout
	 * @return
	 */
	public static String postAttachment(String url, Collection<NameValuePair> parameters,
			Collection<FileNameValuePair> attachments, Charset characterSet, int timeout) {
		// 入参检查
		if (StringUtils.isBlank(url)) {
			return null;
		}

		Collection<NameValuePair> params = parameters;
		if (null == params) {
			params = new ArrayList<NameValuePair>();
		}
		Collection<FileNameValuePair> files = attachments;
		if (null == files) {
			files = new ArrayList<FileNameValuePair>();
		}

		int overtime = timeout * 1000;
		if (timeout <= 0) {
			overtime = defaultTimeout * 1000;
		}

		Charset charset = characterSet;
		if (null == charset) {
			charset = defaultCharset;
		}

		String methodName = "postAttachment";
		String responseContent = null;
		HttpPost post = new HttpPost(url);

		// 设置请求配置
		post.setConfig(getRequestConfig(overtime));

		try {
			// 设置请求数据
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();

			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);
			// builder.setLaxMode();

			// 设置表单流数据
			for (FileNameValuePair item : files) {
				// FileBody file = new FileBody(item.getValue());
				// builder.addPart(item.getName(), file);
				builder.addBinaryBody(item.getName(), item.getValue(), item.getContentType(), item.getFileName());
			}

			// 设置表单普通数据
			for (NameValuePair item : params) {
				// StringBody content = new StringBody(item.getValue(), ContentType.TEXT_PLAIN);
				// builder.addPart(item.getName(), content);
				builder.addTextBody(item.getName(), item.getValue());
			}

			HttpEntity requestEntity = builder.build();
			post.setEntity(requestEntity);

			// 设置头信息
			// List<BasicHeader> headers = new ArrayList<BasicHeader>();
			// headers.add(new BasicHeader(HttpHeaders.REFERER, "http://www.ip138.com/ips138.asp"));
			// headers.add(new BasicHeader(HttpHeaders.HOST, "www.ip138.com"));
			// post.setHeaders(headers.toArray(new BasicHeader[headers.size()]));

			if (logger.isDebugEnabled()) {
				List<Map<String, String>> listFiles = new ArrayList<Map<String, String>>();
				for (FileNameValuePair item : files) {
					Map<String, String> mapFile = new HashMap<String, String>();
					mapFile.put("name", item.getName());
					mapFile.put("value", item.getValue().getAbsolutePath());
					listFiles.add(mapFile);
				}

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("parameters", parameters);
				map.put("files", listFiles);
				String data = JSON.toJSONString(map, SerializerFeature.WriteDateUseDateFormat);

				logger.debug(patternInfo, methodName, "request", url, data);
			}

			// 发出请求
			responseContent = submitRequest(post, charset);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			post.releaseConnection();
		}

		if (logger.isDebugEnabled()) {
			logger.debug(patternInfo, methodName, "response", url, responseContent);
		}

		return responseContent;
	}

	/**
	 * 将请求地址和请求参数转换为GET请求方式的请求地址
	 * 
	 * @param url
	 * @param params
	 * @param characterSet
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String convertUrl4Get(String url, Collection<NameValuePair> params, Charset characterSet) {
		// 入参检查
		if (StringUtils.isBlank(url)) {
			return url;
		}

		if (null == params) {
			return url;
		}

		Charset charset = characterSet;
		if (null == charset) {
			charset = defaultCharset;
		}

		// 构造 URI
		StringBuilder uri = new StringBuilder();
		uri.append(url);

		int i = 0;
		for (NameValuePair item : params) {
			if (i == 0 && url.contains("?") == false) {
				uri.append("?");
			} else {
				uri.append("&");
			}

			uri.append(item.getName());
			uri.append("=");

			String value = item.getValue();
			try {
				uri.append(URLEncoder.encode(value, charset.name()));
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage());
				uri.append(URLEncoder.encode(value));
			}

			i++;
		}

		return uri.toString();
	}

	/**
	 * 将请求地址和请求参数转换为GET请求方式的请求地址（字符集默认为UTF-8）
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String convertUrl4Get(String url, Collection<NameValuePair> params) {
		return convertUrl4Get(url, params, defaultCharset);
	}

	/**
	 * GET方式提交数据
	 * 
	 * @param url
	 *            请求地址
	 * @param parameters
	 *            数据集合
	 * @param characterSet
	 *            字符集
	 * @param timeout
	 *            超时时间（单位：秒）
	 * @return
	 */
	public static String invokeGet(String url, Collection<NameValuePair> parameters, Charset characterSet, int timeout) {
		// 入参检查
		if (StringUtils.isBlank(url)) {
			return null;
		}

		Collection<NameValuePair> params = parameters;
		if (null == parameters) {
			parameters = new ArrayList<NameValuePair>();
		}

		int overtime = timeout * 1000;
		if (timeout <= 0) {
			overtime = defaultTimeout * 1000;
		}

		Charset charset = characterSet;
		if (null == charset) {
			charset = defaultCharset;
		}

		String methodName = "invokeGet";
		String responseContent = null;

		// 构造 URI
		String uri = convertUrl4Get(url, params, defaultCharset);
		HttpGet get = new HttpGet(uri);

		// 设置请求配置
		get.setConfig(getRequestConfig(overtime));

		if (logger.isDebugEnabled()) {
			String data = JSON.toJSONString(params, SerializerFeature.WriteDateUseDateFormat);
			logger.debug(patternInfo, methodName, "request", url, data);
		}

		try {
			// 发出请求
			responseContent = submitRequest(get, charset);
		} finally {
			get.releaseConnection();
		}

		if (logger.isDebugEnabled()) {
			logger.debug(patternInfo, methodName, "response", url, responseContent);
		}

		return responseContent;
	}

	public static byte[] downloadAttachment(String url, Collection<NameValuePair> parameters, int timeout) {
		// 入参检查
		if (StringUtils.isBlank(url)) {
			return null;
		}

		Collection<NameValuePair> params = parameters;
		if (null == parameters) {
			parameters = new ArrayList<NameValuePair>();
		}

		int overtime = timeout * 1000;
		if (timeout <= 0) {
			overtime = defaultTimeout * 1000;
		}

		String methodName = "downloadAttachment";
		byte[] responseByteArray = null;

		// 构造 URI
		String uri = convertUrl4Get(url, params, defaultCharset);
		HttpGet get = new HttpGet(uri);

		// 设置请求配置
		get.setConfig(getRequestConfig(overtime));

		if (logger.isDebugEnabled()) {
			String data = JSON.toJSONString(params, SerializerFeature.WriteDateUseDateFormat);
			logger.debug(patternInfo, methodName, "request", url, data);
		}

		try {
			// 发出请求
			responseByteArray = submitRequest(get);
		} finally {
			get.releaseConnection();
		}

		if (logger.isDebugEnabled()) {
			logger.debug(patternInfo, methodName, "response", url, responseByteArray.toString());
		}

		return responseByteArray;
	}

	public static String postJsonBody(String url, String json, Charset charset) {
		return postJsonBody(url, json, charset, defaultTimeout);
	}

	public static String postJsonBody(String url, String json, int timeout) {
		return postJsonBody(url, json, defaultCharset, timeout);
	}

	public static String postJsonBody(String url, String json) {
		return postJsonBody(url, json, defaultCharset, defaultTimeout);
	}

	public static String postForm(String url, Collection<NameValuePair> params, Charset charset) {
		return postForm(url, params, charset, defaultTimeout);
	}

	public static String postForm(String url, Collection<NameValuePair> params, int timeout) {
		return postForm(url, params, defaultCharset, timeout);
	}

	public static String postForm(String url, Collection<NameValuePair> params) {
		return postForm(url, params, defaultCharset, defaultTimeout);
	}

	public static String postAttachment(String url, Collection<NameValuePair> parameters,
			Collection<FileNameValuePair> attachments, Charset characterSet) {
		return postAttachment(url, parameters, attachments, characterSet, defaultTimeout);
	}

	public static String postAttachment(String url, Collection<NameValuePair> parameters,
			Collection<FileNameValuePair> attachments, int timeout) {
		return postAttachment(url, parameters, attachments, defaultCharset, timeout);
	}

	public static String postAttachment(String url, Collection<NameValuePair> parameters,
			Collection<FileNameValuePair> attachments) {
		return postAttachment(url, parameters, attachments, defaultCharset, defaultTimeout);
	}

	public static String invokeGet(String url, Collection<NameValuePair> params, Charset charset) {
		return invokeGet(url, params, charset, defaultTimeout);
	}

	public static String invokeGet(String url, Collection<NameValuePair> params, int timeout) {
		return invokeGet(url, params, defaultCharset, timeout);
	}

	public static String invokeGet(String url, Collection<NameValuePair> params) {
		return invokeGet(url, params, defaultCharset, defaultTimeout);
	}

	public static String invokeGet(String url) {
		return invokeGet(url, null, defaultCharset, defaultTimeout);
	}

	public static byte[] downloadAttachment(String url, Collection<NameValuePair> parameters) {
		return downloadAttachment(url, parameters, defaultTimeout);
	}

	public static byte[] downloadAttachment(String url, int timeout) {
		return downloadAttachment(url, null, timeout);
	}

	public static byte[] downloadAttachment(String url) {
		return downloadAttachment(url, null, defaultTimeout);
	}

	public static void main(String[] args) {
		// TODO 标记程序入口
		// String url = "http://localhost:8080/qc-webapp/qcapi.do";
		// List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		// params.add(new BasicNameValuePair("openid", "oZqj-jvUqTttrCPj4bXDJ7k1S34g"));
		// params.add(new BasicNameValuePair("passport", "b9883a631fa5a64d8dcd1534b047fdf8"));
		// params.add(new BasicNameValuePair("secactionret", "/hfz/HfzSpecialAction/list"));
		// params.add(new BasicNameValuePair("temp", "temp1"));
		// params.add(new BasicNameValuePair("temp", "temp2"));
		// params.add(new BasicNameValuePair("temp", "temp3"));
		// String response = HttpClientUtil.invokeGet(url, params);
		// String response = HttpClientUtil.postForm(url, params);
		// String response = HttpClientUtil.postJsonBody(url, JsonUtils.toJson(params, false, false));
		// System.out.println("getAccessToken ===>> " + response);

		// String filePath = "C:\\Users\\Michael\\Pictures\\logo.jpg";
		// String imgurl = uploadLogo("buffer", filePath);
		// System.out.println(imgurl);

		// queryIpRegion();
		// grabWeb();
		// downloadImages();
		// statIpLog();
		// sort();
		// stock();
		// String response = invokeGet("https://www.baidu.com/baidu?wd=wtf");
		String url = "https://www.baidu.com/baidu?wd=wtf";
		// String url = "http://stock.cnstock.com/live";
		String response = invokeGet(url);
		System.out.println(response);
	}

	public static void stock() {
		final int limit = 30;
		final String KEY_TIME = "time";
		final String KEY_URL = "url";
		final String KEY_LABEL = "label";
		final String KEY_TITLE = "title";
		final String KEY_TITLE_BAK = "titleBak";
		final String KEY_DESCRIBE = "describe";

		final String VALUE_LABEL = "#未知#";

		// 正则表达式
		StringBuffer regex = new StringBuffer();
		regex.append("(?:<li.*?>\\s*<div class=\"title\">\\s*<span class=\"time\">)");
		regex.append("(?<" + KEY_TIME + ">[\\s\\S]*?)");
		regex.append("(?:</span>\\s*<a href=\")");
		regex.append("(?<" + KEY_URL + ">[\\s\\S]*?)");
		regex.append("(?:\" target=\"_blank\" title=\")");
		regex.append("(?<" + KEY_TITLE_BAK + ">[\\s\\S]*?)");
		regex.append("(\"><font.*?>)");
		regex.append("(?<" + KEY_LABEL + ">[\\s\\S]*?)");
		regex.append("(?:</font>)");
		regex.append("(?<" + KEY_TITLE + ">[\\s\\S]*?)");
		regex.append("(?:</a>\\s*<span.*?>.*?</span>\\s*</div>\\s*<div class=\"detail\".*?>\\s*<p class=\"des\">)");
		regex.append("(?<" + KEY_DESCRIBE + ">[\\s\\S]*?)");
		regex.append("(?:<a href=\")");
		regex.append("(?<ignoreUrl>[\\s\\S]*?)");
		regex.append("(?:\" target=\"_blank\">.*?</a></p>)");

		final Pattern pattern = Pattern.compile(regex.toString());
		final Pattern pattern2 = Pattern.compile("^(#[\\u4E00-\\u9FFF]{2}#)|(#PK台#)$");

		try {
			String host = "http://stock.cnstock.com/live";
			String response = invokeGet(host, null);
			if (StringUtils.isBlank(response)) {
				throw new Exception("响应报文获取失败");
			}

			int start = response.indexOf("<div class=\"left-side\">");
			int end = response.indexOf("<div class=\"right-side\">");
			if (-1 == start || -1 == end) {
				throw new Exception("响应报文分割失败");
			}
			String message = response.substring(start, end);
			// System.out.println(message);

			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			Matcher matcher = pattern.matcher(message);
			int i = 1;
			while (matcher.find()) {
				Map<String, String> map = new HashMap<String, String>();

				extract(matcher, map, KEY_TIME, i);
				extract(matcher, map, KEY_URL, i);
				extract(matcher, map, KEY_TITLE, i);
				extract(matcher, map, KEY_LABEL, i);
				extract(matcher, map, KEY_DESCRIBE, i);

				String label = map.get(KEY_LABEL);
				Matcher matcher2 = pattern2.matcher(label);
				if (matcher2.matches() == false) {
					map.put(KEY_LABEL, VALUE_LABEL);
				}

				String title = map.get(KEY_TITLE);
				if (StringUtils.isBlank(title)) {
					title = extract(matcher, null, KEY_TITLE_BAK, i);
					map.put(KEY_TITLE, title);
				}

				String p = "第【{0}】条——标签：【{1}】，标题：【{2}】。";
				System.out.println(MessageFormat.format(p, i, map.get(KEY_LABEL), map.get(KEY_TITLE)));
				list.add(map);

				i++;
			}

			if (list.size() < limit) {
				System.err.println("解析报文异常：数据条数异常，只解析出【" + list.size() + "】条数据。");
				// System.err.println("================================= ↓ 响应报文 ↓ =================================");
				// System.err.println(response);
				// System.err.println("================================= ↑ 响应报文 ↑ =================================");
			}
			// System.out.println(list);
			// Thread.currentThread().sleep(1000L);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String extract(Matcher matcher, Map<String, String> map, String key, int i) {
		if (null == matcher) {
			return "";
		}
		if (StringUtils.isBlank(key)) {
			return "";
		}

		final String errMsg = "解析报文异常：解析第【{0}】条数据的【{1}】属性为空。";

		String value = matcher.group(key);
		if (StringUtils.isNotBlank(value)) {
			// 过滤空格
			value = value.trim();
			// non-breaking space
			value = value.replaceAll("&nbsp;", "");
			value = value.replaceAll("&#160;", "");
			// en space
			value = value.replaceAll("&ensp;", "");
			value = value.replaceAll("&#8194;", "");
			// em space
			value = value.replaceAll("&emsp;", "");
			value = value.replaceAll("&#8195;", "");
			// thin space
			value = value.replaceAll("&thinsp;", "");
			value = value.replaceAll("&#8201;", "");
			// zero width non-joiner
			value = value.replaceAll("&zwnj;", "");
			value = value.replaceAll("&#8204;", "");

			if (null != map) {
				map.put(key, value);
			}
		} else {
			System.err.println(MessageFormat.format(errMsg, i, key));
		}

		return value;
	}

	public static void downloadImages() {
		List<String> data = new ArrayList<String>();

		try {
			String filePath = "C:\\Users\\Michael\\Desktop\\webdata.txt";
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtils.isBlank(line)) {
					continue;
				}

				data.add(line.trim());
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String dicPath = "C:\\Users\\Michael\\Desktop\\images";
		for (String str : data) {
			String[] items = str.split("\t");
			String id = items[0];
			String url = items[3];
			String suffix = url.substring(url.lastIndexOf("."));
			String fileName = id + suffix;

			FileOutputStream fos = null;
			try {
				byte[] byteArray = HttpClientUtil.downloadAttachment(url);
				String filePath = dicPath + File.separator + fileName;
				fos = new FileOutputStream(filePath);
				IOUtils.write(byteArray, fos);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (null != fos) {
					IOUtils.closeQuietly(fos);
				}
			}
		}
	}

	public static void grabWeb() {
		final String KEY_ID = "id";
		final String KEY_TITLE = "title";
		final String KEY_DESC = "desc";
		final String KEY_PIC = "pic";
		final String reg1 = "(<img src=\")(.*?)(\" class=\"i-car\" />)";
		final String reg2 = "(<p class=\"i-result-desc\"><span>)(.*?)(</span><span>)(.*?)(</span><span>)(.*?)(</span></p>)";
		final Pattern p1 = Pattern.compile(reg1);
		final Pattern p2 = Pattern.compile(reg2);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();

		try {
			// 扒取数据
			String pattern = "http://mcp.gdwynns.com/app/index.php?i=1&c=entry&tid=26&id={0}&do=item&m=ceshi&var=%E5%80%92%E6%98%AF";
			for (int i = 0; i < 1000; i++) {
				String url = MessageFormat.format(pattern, i);
				String response = HttpClientUtil.invokeGet(url, null);
				if (StringUtils.isNotBlank(response)) {
					Map<String, String> map = new HashMap<String, String>();
					Matcher m1 = p1.matcher(response);
					Matcher m2 = p2.matcher(response);

					while (m1.find()) {
						map.put(KEY_PIC, m1.group(2));
					}
					while (m2.find()) {
						String title = m2.group(4);
						String desc = m2.group(6);

						map.put(KEY_TITLE, title);
						if (StringUtils.isBlank(desc)) {
							map.put(KEY_DESC, "null");
						} else {
							map.put(KEY_DESC, desc);
						}
					}

					if (map.isEmpty()) {
						continue;
					}

					map.put(KEY_ID, Integer.valueOf(i).toString());
					result.add(map);
				}

				Thread.currentThread().sleep(300L);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(String.format("共读取数据 %s 条。", result.size()));

		try {
			// 数据写入文件
			String filePath = "C:\\Users\\Michael\\Desktop\\webdata.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			writer.write("");
			writer.flush();

			for (Map<String, String> item : result) {
				String id = item.get(KEY_ID);
				if (StringUtils.isBlank(id)) {
					writer.write("null");
				} else {
					writer.write(id);
				}
				writer.write("\t");

				String title = item.get(KEY_TITLE);
				if (StringUtils.isBlank(title)) {
					writer.write("null");
				} else {
					writer.write(title);
				}
				writer.write("\t");

				String desc = item.get(KEY_DESC);
				if (StringUtils.isBlank(desc)) {
					writer.write("null");
				} else {
					writer.write(desc);
				}
				writer.write("\t");

				String pic = item.get(KEY_PIC);
				if (StringUtils.isBlank(pic)) {
					writer.write("null");
				} else {
					writer.write(pic);
				}
				writer.write("\n");
			}

			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void statIpLog() {
		final String KEY_OPENID = "openid";
		final String KEY_TYPE = "type";
		final String KEY_IP = "ip";
		final String KEY_REGION = "region";
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();

		try {
			String filePath = "C:\\Users\\Michael\\Desktop\\test.txt";
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			List<String> logs = new ArrayList<String>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtils.isBlank(line)) {
					continue;
				}

				logs.add(line.trim());
			}
			reader.close();

			final String param = "{\"openid\":\"sykxm_info\",\"passport\":\"822cdcdd8ed3f611ce8d99b8dceb6d5e\",\"action\":\"/global/Qqcms/queryRegionByIpAddress\",\"requestParam\":{\"ip\":\"%s\"}}";
			final String reg = "(\\+0800)(.*?)(/weixin-green/wxapi.do)(.*?)(%22openid%22%3A%22)(.*?)(%22%2C%22)";
			final Pattern pattern = Pattern.compile(reg);
			String url = "http://htzs.dev.wx.webhante.com/qc-webapp/qcapi.do";

			for (String log : logs) {
				if (StringUtils.isNotBlank(log)) {
					Map<String, String> map = new HashMap<String, String>();
					Matcher matcher = pattern.matcher(log);

					while (matcher.find()) {
						String type = matcher.group(2).trim();
						String openid = matcher.group(6);

						map.put(KEY_TYPE, type);
						map.put(KEY_OPENID, openid);

						String[] arr = log.split(" - ");
						String ip = arr[0];
						map.put(KEY_IP, ip);

						Collection<NameValuePair> params = new ArrayList<NameValuePair>();
						BasicNameValuePair pair = new BasicNameValuePair("j", String.format(param, ip));
						params.add(pair);
						String response = HttpClientUtil.postForm(url, params, 60);
						System.out.println(response);
					}

					if (map.isEmpty()) {
						continue;
					}

					result.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(String.format("共读取有效数据 %s 条。", result.size()));

		try {
			// 数据写入文件
			String filePath = "C:\\Users\\Michael\\Desktop\\result.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			writer.write("");
			writer.flush();

			for (Map<String, String> item : result) {
				String openid = item.get(KEY_OPENID);
				if (StringUtils.isBlank(openid)) {
					writer.write("null");
				} else {
					writer.write(openid);
				}
				writer.write("\t");

				String ip = item.get(KEY_IP);
				if (StringUtils.isBlank(ip)) {
					writer.write("null");
				} else {
					writer.write(ip);
				}
				writer.write("\t");

				String region = item.get(KEY_REGION);
				if (StringUtils.isBlank(region)) {
					writer.write("null");
				} else {
					writer.write(region);
				}
				writer.write("\t");

				String type = item.get(KEY_TYPE);
				if (StringUtils.isBlank(type)) {
					writer.write("null");
				} else {
					writer.write(type);
				}
				writer.write("\n");
			}

			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void queryIpRegion() {
		String filePath = "C:\\Users\\Michael\\Desktop\\ipdata.csv";
		// String filePath = "C:\\Users\\Michael\\Desktop\\temp.txt";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			List<String> ips = new ArrayList<String>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtils.isBlank(line)) {
					continue;
				}

				ips.add(line.trim());
			}
			reader.close();

			String url = "http://www.ip138.com/ips138.asp";
			String reg = "(<td align=\"center\"><ul class=\"ul1\"><li>)(.*?)(</li><li>)";
			Pattern p = Pattern.compile(reg);
			Map<String, String> result = new HashMap<String, String>();

			for (String ip : ips) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("action", "2"));
				params.add(new BasicNameValuePair("ip", ip));

				// 延时
				// Thread.currentThread().sleep(500);
				Charset charset = Charset.forName("gb2312");
				String response = HttpClientUtil.invokeGet(url, params, charset);

				if (StringUtils.isNotBlank(response)) {
					Matcher m = p.matcher(response);
					while (m.find()) {
						result.put(ip, m.group(2).trim());
					}
				}
			}

			String filePath2 = "C:\\Users\\Michael\\Desktop\\ipregion.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath2));
			writer.write("");
			writer.flush();

			for (Entry<String, String> item : result.entrySet()) {
				writer.write(item.getKey());
				writer.write("\t");
				writer.write(item.getValue());
				writer.write("\n");
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getAccessToken(String appid, String secret) {
		String url = "https://api.weixin.qq.com/cgi-bin/token";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("grant_type", "client_credential"));
		params.add(new BasicNameValuePair("appid", appid));
		params.add(new BasicNameValuePair("secret", secret));

		String response = HttpClientUtil.invokeGet(url, params);
		System.out.println("getAccessToken ----->> " + response);

		Map<String, Object> result = JSON.parseObject(response, new TypeReference<Map<String, Object>>() {});

		return (String) result.get("access_token");
	}

	public static String uploadLogo(String name, String filePath) {
		String accessToken = getAccessToken("wx118ae674f0911ba8", "945b1b09a3ac412836669c7a4465f097");
		// String url = "http://file.api.weixin.qq.com/cgi-bin/media/upload";
		String url = "http://api.weixin.qq.com/cgi-bin/media/uploadimg";
		// String url = "http://127.0.0.1:8080/qc-webapp/qcapi.do";

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("access_token", accessToken));
		params.add(new BasicNameValuePair("type", "image"));

		File file = new File(filePath);
		if (file.exists() == false || file.isFile() == false) {
			return null;
		}
		List<FileNameValuePair> files = new ArrayList<FileNameValuePair>();
		files.add(new FileNameValuePair(name, file));
		File gif = new File("C:\\Users\\Michael\\Pictures\\children.gif");
		files.add(new FileNameValuePair("gif", gif));

		// String uri = convertUrl4Get(url, params);

		String responseContent = HttpClientUtil.postAttachment(url, params, files);

		return responseContent;
	}

}
