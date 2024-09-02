package com.mawen.learn.mybatis.mapping;

import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/1
 */
public class ResultMap {

	private Configuration configuration;

	private String id;
	private Class<?> type;
	private List<ResultMapping> resultMappings;
	private List<ResultMapping> idResultMappings;
}
