package org.mas.stock.dal.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.mas.stock.dal.model.NewsDO;

public interface NewsMapper {
	int addNews(NewsDO newsDO);

	int updateNews(NewsDO newsDO);

	NewsDO selectById(long id);

	NewsDO selectByUrl(String url);

	boolean existByUrl(@Param("url") String url);

	List<NewsDO> pagedNews(@Param("condition") NewsDO condition, @Param("offset") Long offset,
			@Param("limit") Long limit);

}
