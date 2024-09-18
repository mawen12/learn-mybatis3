package com.mawen.learn.mybatis.scripting.defaults;

import java.util.HashMap;

import com.mawen.learn.mybatis.builder.SqlSourceBuilder;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.SqlSource;
import com.mawen.learn.mybatis.scripting.xmltags.DynamicContext;
import com.mawen.learn.mybatis.scripting.xmltags.SqlNode;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/18
 */
public class RawSqlSource implements SqlSource {

	private final SqlSource sqlSource;

	public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
		this(configuration, getSql(configuration, rootSqlNode), parameterType);
	}

	public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
		SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
		Class<?> clazz = parameterType == null ? Object.class : parameterType;
		sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
	}

	@Override
	public BoundSql getBoundSql(Object parameterObject) {
		return sqlSource.getBoundSql(parameterObject);
	}

	private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
		DynamicContext context = new DynamicContext(configuration, null);
		rootSqlNode.apply(context);
		return context.getSql();
	}
}
