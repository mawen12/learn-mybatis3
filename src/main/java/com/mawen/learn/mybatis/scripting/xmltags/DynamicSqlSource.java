package com.mawen.learn.mybatis.scripting.xmltags;

import com.mawen.learn.mybatis.builder.SqlSourceBuilder;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.SqlSource;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/18
 */
public class DynamicSqlSource implements SqlSource {

	private final Configuration configuration;
	private final SqlNode rootSqlNode;

	public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
		this.configuration = configuration;
		this.rootSqlNode = rootSqlNode;
	}

	@Override
	public BoundSql getBoundSql(Object parameterObject) {
		DynamicContext context = new DynamicContext(configuration, parameterObject);
		rootSqlNode.apply(context);
		SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
		Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
		SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
		BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
		context.getBindings().forEach(boundSql::setAdditionalParameter);
		return boundSql;
	}
}
