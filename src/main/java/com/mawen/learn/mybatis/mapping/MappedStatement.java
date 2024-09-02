package com.mawen.learn.mybatis.mapping;

import com.mawen.learn.mybatis.session.Configuration;
import sun.security.util.Cache;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public final class MappedStatement {

	private String resource;
	private Configuration configuration;
	private String id;
	private Integer fetchSize;
	private Integer timeout;
	private StatementType statementType;
	private ResultSetType resultSetType;
	private SqlSource sqlSource;
	private Cache cache;
	private ParameterMap parameterMap;
	private List<ResultMap>

}
