package com.mawen.learn.mybatis.mapping;

import java.util.List;

import com.mawen.learn.mybatis.executor.keygen.KeyGenerator;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.scripting.LanguageDriver;
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
	private List<ResultMap> resultMaps;
	private boolean flushCacheRequired;
	private boolean resultOrdered;
	private SqlCommandType sqlCommandType;
	private KeyGenerator keyGenerator;
	private String[] keyProperties;
	private String[] keyColumns;
	private boolean hasNestedResultMaps;
	private String databaseId;
	private Log statementLog;
	private LanguageDriver lang;
	private String[] resultSets;
}
