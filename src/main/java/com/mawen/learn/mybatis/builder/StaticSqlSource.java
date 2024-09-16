package com.mawen.learn.mybatis.builder;

import java.util.List;

import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.ParameterMapping;
import com.mawen.learn.mybatis.mapping.SqlSource;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class StaticSqlSource implements SqlSource {

	private final String sql;
	private final List<ParameterMapping> parameterMappings;
	private final Configuration configuration;

	public StaticSqlSource(Configuration configuration, String sql) {
		this(configuration, sql, null);
	}

	public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
		this.sql = sql;
		this.parameterMappings = parameterMappings;
		this.configuration = configuration;
	}

	@Override
	public BoundSql getBoundSql(Object parameterObject) {
		return new BoundSql(configuration, sql, parameterMappings, parameterObject);
	}
}
