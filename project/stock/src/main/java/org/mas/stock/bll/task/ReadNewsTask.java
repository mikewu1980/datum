package org.mas.stock.bll.task;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mas.stock.bll.model.NewsBO;
import org.mas.stock.bll.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

@Component
public class ReadNewsTask {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("#{SYSTEM_CONFIG['stock.news.url']}")
	private String url;
	@Resource
	private NewsService newsService;

	/**
	 * 定时从网站读取新闻，然后写入数据库
	 */
	@Scheduled(cron = "*/5 * * * * ?")
	public void pullNews() {
		logger.info("pullNews start...");

		try {
			Document document = request(url);

			List<NewsBO> newsList = parseNewsList(document);

			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			for (NewsBO item : newsList) {
				Document doc = request(item.getUrl());
				if (null != doc) {
					// 读取发布时间
					Elements spanList = doc.select("div#pager-content div.bullet span.timer");
					if (CollectionUtils.isNotEmpty(spanList)) {
						String publishTime = spanList.get(0).text();
						publishTime = org.mas.stock.utils.StringUtils.trim(publishTime);
						DateTime dateTime = DateTime.parse(publishTime, formatter);
						item.setPublishTime(dateTime.toString());
					}

					if (item.isTradeData()) {
						Elements pList = doc.select("div#pager-content div#qmt_content_div p");
						if (CollectionUtils.isNotEmpty(pList)) {
							pList.remove(0);
							StringBuffer content = new StringBuffer();
							for (Element p : pList) {
								if (content.length() > 0) {
									content.append("\n");
								}
								String temp = org.mas.stock.utils.StringUtils.trim(p.text());
								content.append(temp);
							}
							item.setContent(content.toString());
						}
					}

					try {
						Thread.currentThread().sleep(500);
					} catch (Exception e) {
					}
				}

				newsService.saveNews(item);
			}
		} catch (Exception e) {
			logger.error("pullNews error.", e);
		}

		logger.info("pullNews end...");
	}

	private Document request(String url) throws Exception {
		if (StringUtils.isBlank(url)) {
			return null;
		}

		try {
			String userAgent = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36";
			int timeout = 1000 * 2;

			org.jsoup.Connection conn = Jsoup.connect(url);
			conn.userAgent(userAgent);
			conn.timeout(timeout);
			Document doc = conn.get();
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private List<NewsBO> parseNewsList(Document doc) throws Exception {
		List<NewsBO> list = new ArrayList<NewsBO>();
		if (null == doc) {
			return list;
		}

		Elements liList = doc.select("ul#zb-list li");
		if (CollectionUtils.isEmpty(liList)) {
			throw new Exception("parse html failure");
		}

		final String VALUE_LABEL = "#未知#";

		for (Element li : liList) {
			NewsBO news = new NewsBO();
			Map<String, String> urlMap = new HashMap<String, String>();

			// 读取链接和标题
			Elements anchorList = li.select("div.title a");
			if (CollectionUtils.isNotEmpty(anchorList)) {
				for (Element anchor : anchorList) {
					String url = anchor.attr("href");
					url = org.mas.stock.utils.StringUtils.trim(url);
					String title = anchor.text();
					title = org.mas.stock.utils.StringUtils.trim(title);
					String temp = capture(title);
					if (StringUtils.isNoneBlank(temp)) {
						title = title.replaceAll(temp, "");
					}

					String title2 = urlMap.get(url);
					if (StringUtils.isBlank(title2)) {
						urlMap.put(url, title);
					} else {
						title = " | " + title2;
						urlMap.put(url, title);
					}
				}
			} else {
				logger.error("url read error. li:{}", li);
				continue;
			}

			// 读取标签
			String label = VALUE_LABEL;
			Elements labelList = li.select("div.title font");
			if (CollectionUtils.isNotEmpty(labelList)) {
				String temp = labelList.get(0).text();
				temp = org.mas.stock.utils.StringUtils.trim(temp);
				temp = capture(temp);
				if (StringUtils.isNoneBlank(temp)) {
					label = temp;
				}
			}
			news.setLabel(label);

			if ("#PK台#".equals(news.getLabel())) {
				news.setTradeData(true);
			}

			// 读取描述
			Elements descList = li.select("p.des");
			if (CollectionUtils.isNotEmpty(descList)) {
				String describe = descList.get(0).text();
				describe = describe.replaceAll("\\[全文\\]", "");
				describe = org.mas.stock.utils.StringUtils.trim(describe);
				news.setDescribe(describe);
			}

			for (Map.Entry<String, String> entry : urlMap.entrySet()) {
				String url = entry.getKey();
				String title = entry.getValue();

				NewsBO clone = news.copyPropertiesTo(new NewsBO());
				clone.setUrl(url);
				clone.setTitle(title);

				list.add(clone);

				if (logger.isDebugEnabled()) {
					String json = JSON.toJSONString(clone, SerializerFeature.PrettyFormat);
					logger.debug(json);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			String msg = MessageFormat.format("数据数量：{0}条", list.size());
			logger.debug(msg);
		}

		return list;
	}

	private String capture(String text) {
		final Pattern pattern = Pattern.compile("(#[\\u4E00-\\u9FFF]{2}#)|(#PK台#)");
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			String label = matcher.group();
			label = org.mas.stock.utils.StringUtils.trim(label);
			return label;
		}

		return null;
	}

}
