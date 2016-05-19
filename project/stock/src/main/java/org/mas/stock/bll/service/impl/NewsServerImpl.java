package org.mas.stock.bll.service.impl;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.mas.stock.bll.model.NewsBO;
import org.mas.stock.bll.service.NewsService;
import org.mas.stock.dal.mapper.NewsMapper;
import org.mas.stock.dal.model.NewsDO;
import org.springframework.stereotype.Service;

@Service
public class NewsServerImpl extends BaseService implements NewsService {
	@Resource
	private NewsMapper newsMapper;

	@Override
	public int saveNews(NewsBO news) {
		if (StringUtils.isAnyBlank(news.getUrl(), news.getLabel(), news.getTitle())) {
			return -1;
		}

		NewsDO newsDO = news.copyPropertiesTo(new NewsDO());
		int result = newsMapper.addNews(newsDO);

		return result;
	}

}
